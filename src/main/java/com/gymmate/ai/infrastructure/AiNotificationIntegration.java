package com.gymmate.ai.infrastructure;

import com.gymmate.ai.domain.AiRecommendation;
import com.gymmate.notification.application.NotificationService;
import com.gymmate.notification.domain.Notification;
import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.user.application.MemberService;
import com.gymmate.user.domain.Member;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiNotificationIntegration {

    private final NotificationService notificationService;
    private final MemberService memberService;
    private final UserRepository userRepository;

    /**
     * @param memberId
     * @param recommendation
     */
    @Transactional
    public void sendAiPlanNotification(UUID memberId, AiRecommendation recommendation) {
        try {
            Member member = memberService.findById(memberId);
            User user = userRepository.findById(member.getUserId()).orElseThrow();

            String title = "Your Personalized AI Gym Plan is Ready!";
            String content = "Hello " + user.getFirstName() + ",\n\n" +
                    "Based on your location and fitness goals, our AI Gym Trainer has crafted a personalized plan for you:\n\n"
                    +
                    "**Workout Plan**\n" + recommendation.getWorkoutPlan() + "\n\n" +
                    "**Meal Plan**\n" + recommendation.getMealPlan();

            // Depending on Notification module architecture, we'll send it
            // Assuming NotificationService has a method to create standard notifications
            // If not, we will save it manually or use EmailService
            // TODO: Implement actual notification sending
            notificationService.sendToUser(memberId, title, content, NotificationPriority.HIGH, content, null);

            // As a fallback, we log it
            log.info("AI Plan generated for user: {}. Content: {}", user.getEmail(), content);

            // If the notification service requires more structure, we will adapt here
        } catch (Exception e) {
            log.error("Error sending AI notification to member {}", memberId, e);
        }
    }
}
