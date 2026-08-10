package com.gymmate.unit.ai.application;

import com.gymmate.ai.api.dto.AiPlanResponse;
import com.gymmate.ai.application.AiPlanService;
import com.gymmate.ai.application.port.LlmClient;
import com.gymmate.ai.domain.AiRecommendation;
import com.gymmate.ai.infrastructure.AiRecommendationRepository;
import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.user.application.MemberService;
import com.gymmate.user.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Confirms {@code AiPlanService} depends on {@link LlmClient} (the port), not a
 * concrete Spring AI {@code ChatClient} — see {@code ai.application.port} package
 * Javadoc.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AiPlanService Unit Tests")
class AiPlanServiceTest {

    @Mock private LlmClient llmClient;
    @Mock private AiRecommendationRepository recommendationRepository;
    @Mock private MemberService memberService;
    @Mock private GymRepository gymRepository;
    @Mock private RedisTemplate<String, Object> redisTemplate;

    private AiPlanService service;

    @BeforeEach
    void setUp() {
        service = new AiPlanService(llmClient, recommendationRepository, memberService, gymRepository, redisTemplate);
        ReflectionTestUtils.setField(service, "cacheTtlHours", 24L);
    }

    @Test
    @DisplayName("regeneratePlan calls LlmClient.complete, not a ChatClient directly")
    void regeneratePlan_callsLlmClientComplete() {
        UUID memberId = UUID.randomUUID();
        UUID gymId = UUID.randomUUID();

        Member member = Member.builder().build();
        member.setId(memberId);
        member.setGymId(gymId);

        Gym gym = Gym.builder().city("Austin").country("USA").build();
        gym.setId(gymId);

        when(memberService.findById(memberId)).thenReturn(member);
        when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));
        when(llmClient.complete(isNull(), anyString()))
                .thenReturn("WORKOUT PLAN: squats and rows\nMEAL PLAN: chicken and rice");
        when(recommendationRepository.save(any(AiRecommendation.class))).thenAnswer(inv -> {
            AiRecommendation saved = inv.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        AiPlanResponse response = service.regeneratePlan(memberId, null);

        verify(llmClient).complete(isNull(), anyString());
        assertThat(response.workoutPlan()).contains("squats and rows");
        assertThat(response.mealPlan()).contains("chicken and rice");
    }
}
