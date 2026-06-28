package com.gymmate.ai.application;

import com.gymmate.ai.api.dto.AiPlanRequest;
import com.gymmate.ai.api.dto.AiPlanResponse;
import com.gymmate.ai.domain.AiRecommendation;
import com.gymmate.ai.infrastructure.AiRecommendationRepository;
import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.user.application.MemberService;
import com.gymmate.user.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * On-demand AI personal trainer service.
 *
 * <p>Flow for GET (my-plan):
 * <ol>
 *   <li>Check Redis cache key {@code ai:plan:{memberId}}.</li>
 *   <li>On hit → return cached {@link AiPlanResponse} (no LLM call).</li>
 *   <li>On miss → load latest persisted {@link AiRecommendation} from DB.</li>
 *   <li>If nothing in DB → generate fresh plan, persist, cache, return.</li>
 * </ol>
 *
 * <p>Flow for POST (generate-plan):
 * <ol>
 *   <li>Optionally update the member's fitness goals and {@code experienceLevel}.</li>
 *   <li>Evict Redis cache.</li>
 *   <li>Call LLM, persist, cache, return.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiPlanService {

    private static final String CACHE_KEY_PREFIX = "ai:plan:";

    private final ChatClient.Builder chatClientBuilder;
    private final AiRecommendationRepository recommendationRepository;
    private final MemberService memberService;
    private final GymRepository gymRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${ai.plan.cache.ttl-hours:24}")
    private long cacheTtlHours;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns the latest AI plan for the given member.
     * Serves from Redis cache when available; falls back to DB or generates on first call.
     */
    @Transactional
    public AiPlanResponse getOrGeneratePlan(UUID memberId) {
        // 1. Cache hit?
        Optional<AiPlanResponse> cached = getFromCache(memberId);
        if (cached.isPresent()) {
            log.debug("AI plan cache HIT for member {}", memberId);
            return cached.get();
        }

        // 2. DB hit?
        Optional<AiRecommendation> existing =
            recommendationRepository.findTopByMemberIdOrderByCreatedAtDesc(memberId);
        if (existing.isPresent()) {
            AiPlanResponse response = AiPlanResponse.fromEntity(existing.get(), false);
            putInCache(memberId, response);
            return response;
        }

        // 3. Nothing at all — generate a brand-new plan using stored goals
        log.info("No existing AI plan for member {} — generating first plan", memberId);
        return generateAndPersist(memberId, null);
    }

    /**
     * Generates (or regenerates) an AI plan, optionally updating the member's stored goals.
     * Always evicts the cache, calls the LLM, persists, and re-caches the result.
     *
     * @param memberId member to generate the plan for
     * @param request  optional goal / experience-level overrides (may be null)
     */
    @Transactional
    public AiPlanResponse regeneratePlan(UUID memberId, AiPlanRequest request) {
        // Optionally persist updated goals on the Member entity
        if (request != null) {
            applyGoalUpdates(memberId, request);
        }

        // Evict stale cache entry
        evictCache(memberId);

        return generateAndPersist(memberId, request);
    }

    /**
     * Returns the full plan history for a member (newest first), without caching.
     */
    @Transactional(readOnly = true)
    public List<AiPlanResponse> getPlanHistory(UUID memberId) {
        return recommendationRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
            .stream()
            .map(r -> AiPlanResponse.fromEntity(r, false))
            .toList();
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private AiPlanResponse generateAndPersist(UUID memberId, AiPlanRequest request) {
        Member member = memberService.findById(memberId);

        Gym gym = gymRepository.findById(member.getGymId())
            .orElseThrow(() -> new ResourceNotFoundException("Gym", member.getGymId().toString()));

        // Resolve goals: request override → stored member goals → sensible default
        List<String> goals = resolveGoals(member, request);
        String experienceLevel = resolveExperienceLevel(member, request);
        String location = buildLocation(gym);

        String prompt = buildPrompt(goals, experienceLevel, location);

        log.info("Calling AI for member {} with goals: {}", memberId, goals);
        String aiResponse;
        try {
            aiResponse = chatClientBuilder.build().prompt().user(prompt).call().content();
        } catch (Exception e) {
            log.error("AI provider call failed for member {}", memberId, e);
            throw new IllegalStateException("AI service is temporarily unavailable. Please try again shortly.", e);
        }

        String workoutPlan = extractSection(aiResponse, "WORKOUT PLAN:", "MEAL PLAN:");
        String mealPlan    = extractSection(aiResponse, "MEAL PLAN:", null);

        AiRecommendation recommendation = AiRecommendation.builder()
            .memberId(memberId)
            .workoutPlan(workoutPlan.trim())
            .mealPlan(mealPlan.trim())
            .goalsUsed(goals.toArray(String[]::new))
            .experienceLevel(experienceLevel)
            .build();
        recommendation.setGymId(member.getGymId());
        recommendation.setOrganisationId(member.getOrganisationId());

        AiRecommendation saved = recommendationRepository.save(recommendation);
        AiPlanResponse response = AiPlanResponse.fromEntity(saved, false);

        putInCache(memberId, response);
        return response;
    }

    private void applyGoalUpdates(UUID memberId, AiPlanRequest request) {
        if (request.fitnessGoals() != null && !request.fitnessGoals().isEmpty()) {
            memberService.updateFitnessGoals(
                memberId,
                request.fitnessGoals().toArray(String[]::new),
                request.experienceLevel()
            );
        } else if (request.experienceLevel() != null) {
            memberService.updateFitnessGoals(memberId, null, request.experienceLevel());
        }
    }

    private List<String> resolveGoals(Member member, AiPlanRequest request) {
        if (request != null && request.fitnessGoals() != null && !request.fitnessGoals().isEmpty()) {
            return request.fitnessGoals();
        }
        if (member.getFitnessGoals() != null && member.getFitnessGoals().length > 0) {
            return Arrays.asList(member.getFitnessGoals());
        }
        return List.of("General fitness and well-being");
    }

    private String resolveExperienceLevel(Member member, AiPlanRequest request) {
        if (request != null && request.experienceLevel() != null) {
            return request.experienceLevel();
        }
        return member.getExperienceLevel() != null ? member.getExperienceLevel() : "beginner";
    }

    private String buildLocation(Gym gym) {
        String city    = gym.getCity()    != null ? gym.getCity()    : "";
        String country = gym.getCountry() != null ? gym.getCountry() : "";
        String location = (city + (city.isBlank() ? "" : ", ") + country).trim();
        return location.isBlank() || location.equals(",") ? "your local area" : location;
    }

    private String buildPrompt(List<String> goals, String experienceLevel, String location) {
        String goalsText = String.join(", ", goals);
        return String.format(
            "You are an expert AI Gym Trainer. " +
            "The member is at %s experience level and lives in %s. " +
            "Their fitness goals are: %s.\n\n" +
            "Provide a response in exactly two labelled sections:\n" +
            "1. WORKOUT PLAN: A structured weekly workout plan tailored to the experience level and goals.\n" +
            "2. MEAL PLAN: A meal plan that MUST feature healthy versions of local cuisine and ingredients " +
            "easily available in %s, aligned with the fitness goals.\n\n" +
            "Be specific, practical, and motivating. " +
            "Include a brief medical disclaimer that this is not professional medical advice.",
            experienceLevel, location, goalsText, location
        );
    }

    private String extractSection(String fullText, String startMarker, String endMarker) {
        int startIndex = fullText.indexOf(startMarker);
        if (startIndex == -1) return "Plan not available.";
        startIndex += startMarker.length();

        if (endMarker != null) {
            int endIndex = fullText.indexOf(endMarker);
            if (endIndex != -1 && endIndex > startIndex) {
                return fullText.substring(startIndex, endIndex);
            }
        }
        return fullText.substring(startIndex);
    }

    // -------------------------------------------------------------------------
    // Redis helpers (graceful degradation on Redis unavailability)
    // -------------------------------------------------------------------------

    private String cacheKey(UUID memberId) {
        return CACHE_KEY_PREFIX + memberId;
    }

    private Optional<AiPlanResponse> getFromCache(UUID memberId) {
        try {
            Object value = redisTemplate.opsForValue().get(cacheKey(memberId));
            if (value instanceof AiPlanResponse plan) {
                // Return with cached=true flag
                return Optional.of(new AiPlanResponse(
                    plan.recommendationId(), plan.memberId(),
                    plan.workoutPlan(), plan.mealPlan(),
                    plan.goalsUsed(), plan.experienceLevel(),
                    plan.generatedAt(), true
                ));
            }
        } catch (Exception e) {
            log.warn("Redis read failed for member {} — falling back to DB: {}", memberId, e.getMessage());
        }
        return Optional.empty();
    }

    private void putInCache(UUID memberId, AiPlanResponse response) {
        try {
            redisTemplate.opsForValue().set(cacheKey(memberId), response, cacheTtlHours, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis write failed for member {} — plan not cached: {}", memberId, e.getMessage());
        }
    }

    private void evictCache(UUID memberId) {
        try {
            redisTemplate.delete(cacheKey(memberId));
        } catch (Exception e) {
            log.warn("Redis eviction failed for member {}: {}", memberId, e.getMessage());
        }
    }
}


