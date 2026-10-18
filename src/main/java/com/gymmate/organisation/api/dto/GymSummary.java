package com.gymmate.organisation.api.dto;

import com.gymmate.shared.constants.GymStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/** Read model of a gym (location), exposed to other modules instead of the Gym aggregate. */
public record GymSummary(
        UUID id,
        UUID organisationId,
        String name,
        String contactEmail,
        String city,
        String country,
        String timezone,
        String currency,
        GymStatus status,
        boolean active,
        String stripeConnectAccountId,
        Boolean stripeChargesEnabled,
        Boolean stripePayoutsEnabled,
        Boolean stripeDetailsSubmitted,
        LocalDateTime stripeOnboardingCompletedAt) {
}
