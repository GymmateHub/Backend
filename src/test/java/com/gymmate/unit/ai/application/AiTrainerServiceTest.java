package com.gymmate.unit.ai.application;

import com.gymmate.ai.application.AiTrainerService;
import com.gymmate.ai.application.port.LlmClient;
import com.gymmate.ai.domain.AiRecommendation;
import com.gymmate.ai.infrastructure.AiNotificationIntegration;
import com.gymmate.ai.infrastructure.AiRecommendationRepository;
import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.user.domain.events.MemberOnboardedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Confirms {@code AiTrainerService} depends on {@link LlmClient} (the port), not a
 * concrete Spring AI {@code ChatClient} — see {@code ai.application.port} package
 * Javadoc.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AiTrainerService Unit Tests")
class AiTrainerServiceTest {

    @Mock private LlmClient llmClient;
    @Mock private AiRecommendationRepository aiRecommendationRepository;
    @Mock private GymRepository gymRepository;
    @Mock private AiNotificationIntegration aiNotificationIntegration;

    @Test
    @DisplayName("handleMemberOnboardedEvent calls LlmClient.complete, not a ChatClient directly")
    void handleMemberOnboardedEvent_callsLlmClientComplete() {
        AiTrainerService service = new AiTrainerService(llmClient, aiRecommendationRepository, gymRepository, aiNotificationIntegration);

        UUID organisationId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID gymId = UUID.randomUUID();

        Gym gym = Gym.builder().city("Austin").country("USA").build();
        gym.setId(gymId);
        when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));
        when(llmClient.complete(isNull(), anyString()))
                .thenReturn("WORKOUT PLAN: squats and rows\nMEAL PLAN: chicken and rice");
        when(aiRecommendationRepository.save(any(AiRecommendation.class))).thenAnswer(inv -> inv.getArgument(0));

        MemberOnboardedEvent event = new MemberOnboardedEvent(
                this, organisationId, memberId, gymId, new String[] { "strength", "endurance" });

        service.handleMemberOnboardedEvent(event);

        verify(llmClient).complete(isNull(), anyString());
        verify(aiNotificationIntegration).sendAiPlanNotification(any(UUID.class), any(AiRecommendation.class));
    }
}
