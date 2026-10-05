package com.gymmate.identity.api.dto;

import com.gymmate.shared.constants.MemberStatus;

import java.util.UUID;

/** Read model of a gym member profile, exposed to other modules instead of the Member aggregate. */
public record MemberProfile(
        UUID id,
        UUID userId,
        UUID organisationId,
        UUID gymId,
        MemberStatus status,
        boolean active,
        boolean waiverSigned,
        String[] fitnessGoals,
        String experienceLevel) {
}
