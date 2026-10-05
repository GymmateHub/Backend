package com.gymmate.ai.internal.application;

import com.gymmate.ai.internal.application.port.LlmClient;
import com.gymmate.ai.internal.domain.AiRecommendation;
import com.gymmate.ai.internal.application.port.AiPlanNotifier;
import com.gymmate.ai.internal.application.port.AiRecommendationRepository;
import com.gymmate.organisation.api.dto.GymSummary;
import com.gymmate.organisation.api.OrganisationApi;
import com.gymmate.shared.multitenancy.TenantScope;
import com.gymmate.identity.api.event.MemberOnboardedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTrainerService {

    private final LlmClient llmClient;
    private final AiRecommendationRepository aiRecommendationRepository;
    private final OrganisationApi organisationApi;
    private final AiPlanNotifier aiNotificationIntegration;

    @Async
    @EventListener
    @Transactional
    public void handleMemberOnboardedEvent(MemberOnboardedEvent event) {
        log.info("Generating AI plan for member {} at gym {}", event.getMemberId(), event.getGymId());

        try (TenantScope ignored = TenantScope.activate(event.getOrganisationId(), event.getGymId())) {
            GymSummary gym = organisationApi.findGym(event.getGymId())
                .orElseThrow(() -> new IllegalStateException("Gym not found"));

            String goals = String.join(", ", event.getFitnessGoals());
            String location = (gym.city() != null ? gym.city() : "") +
                              (gym.country() != null ? ", " + gym.country() : "");

            if (location.trim().isEmpty() || location.equals(",")) {
                location = "your local area";
            }

            String prompt = String.format(
                "You are an expert AI Gym Trainer. The user lives in %s and wants to achieve the following fitness goals: %s. " +
                "Please provide a response in exactly two sections:\n" +
                "1. WORKOUT PLAN: A weekly workout plan tailored to these goals.\n" +
                "2. MEAL PLAN: A meal plan that MUST heavily feature healthy versions of local cuisine and easily accessible local ingredients from %s.",
                location, goals, location
            );

            String response;
            try {
                response = llmClient.complete(null, prompt);
            } catch (Exception e) {
                log.error("Failed to call AI provider", e);
                return;
            }

            String workoutPlan = extractSection(response, "WORKOUT PLAN:", "MEAL PLAN:");
            String mealPlan = extractSection(response, "MEAL PLAN:", null);

            AiRecommendation recommendation = AiRecommendation.builder()
                    .memberId(event.getMemberId())
                    .workoutPlan(workoutPlan.trim())
                    .mealPlan(mealPlan.trim())
                    .build();

            recommendation.setGymId(event.getGymId());

            aiRecommendationRepository.save(recommendation);

            // Notify user
            aiNotificationIntegration.sendAiPlanNotification(event.getMemberId(), recommendation);
        }
    }

    private String extractSection(String fullText, String startMarker, String endMarker) {
        int startIndex = fullText.indexOf(startMarker);
        if (startIndex == -1) return "Plan not available.";
        startIndex += startMarker.length();

        if (endMarker != null) {
            int endIndex = fullText.indexOf(endMarker);
            if (endIndex != -1) {
                return fullText.substring(startIndex, endIndex);
            }
        }
        return fullText.substring(startIndex);
    }
}
