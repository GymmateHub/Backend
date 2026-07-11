package com.gymmate.ai.application;

import com.gymmate.ai.domain.AiRecommendation;
import com.gymmate.ai.infrastructure.AiNotificationIntegration;
import com.gymmate.ai.infrastructure.AiRecommendationRepository;
import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.user.domain.events.MemberOnboardedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTrainerService {

    private final ChatClient.Builder chatClientBuilder;
    private final AiRecommendationRepository aiRecommendationRepository;
    private final GymRepository gymRepository;
    private final AiNotificationIntegration aiNotificationIntegration;

    @Async
    @EventListener
    @Transactional
    public void handleMemberOnboardedEvent(MemberOnboardedEvent event) {
        log.info("Generating AI plan for member {} at gym {}", event.getMemberId(), event.getGymId());

        Gym gym = gymRepository.findById(event.getGymId())
            .orElseThrow(() -> new IllegalStateException("Gym not found"));

        String goals = String.join(", ", event.getFitnessGoals());
        String location = (gym.getCity() != null ? gym.getCity() : "") + 
                          (gym.getCountry() != null ? ", " + gym.getCountry() : "");
        
        if (location.trim().isEmpty() || location.equals(",")) {
            location = "your local area";
        }

        ChatClient chatClient = chatClientBuilder.build();

        String prompt = String.format(
            "You are an expert AI Gym Trainer. The user lives in %s and wants to achieve the following fitness goals: %s. " +
            "Please provide a response in exactly two sections:\n" +
            "1. WORKOUT PLAN: A weekly workout plan tailored to these goals.\n" +
            "2. MEAL PLAN: A meal plan that MUST heavily feature healthy versions of local cuisine and easily accessible local ingredients from %s.",
            location, goals, location
        );

        String response;
        try {
            response = chatClient.prompt().user(prompt).call().content();
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
