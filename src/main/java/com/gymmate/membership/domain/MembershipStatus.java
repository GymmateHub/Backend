package com.gymmate.membership.domain;

public enum MembershipStatus {
    ACTIVE,
    PAST_DUE,
    PAUSED,
    CANCELLED,
    EXPIRED,
    // Suspended after PAST_DUE exceeds the grace period (see MembershipService.escalatePastDueMemberships) —
    // distinct from EXPIRED (which means the membership term itself ran out).
    SUSPENDED
}

