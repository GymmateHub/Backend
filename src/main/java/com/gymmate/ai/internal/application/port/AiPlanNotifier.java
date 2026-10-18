package com.gymmate.ai.internal.application.port;

import com.gymmate.ai.internal.domain.AiRecommendation;

import java.util.UUID;

/** Outbound port: tells a member their AI plan is ready. */
public interface AiPlanNotifier {

    void sendAiPlanNotification(UUID memberId, AiRecommendation recommendation);
}
