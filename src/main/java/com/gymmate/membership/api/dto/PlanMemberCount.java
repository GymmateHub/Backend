package com.gymmate.membership.api.dto;

/** Number of active members on a membership plan. */
public record PlanMemberCount(String planName, long activeMembers) {
}
