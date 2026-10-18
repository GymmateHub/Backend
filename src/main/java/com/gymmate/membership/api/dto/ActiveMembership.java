package com.gymmate.membership.api.dto;

import java.util.UUID;

/** A member's currently active membership, as seen by other modules. */
public record ActiveMembership(UUID id, UUID memberId, UUID membershipPlanId, Integer classCreditsRemaining) {
}
