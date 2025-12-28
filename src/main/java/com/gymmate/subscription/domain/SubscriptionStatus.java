package com.gymmate.subscription.domain;

public enum SubscriptionStatus {
    TRIAL("Trial period"),
    ACTIVE("Active subscription"),
    PAST_DUE("Payment past due"),
    CANCELLED("Cancelled subscription"),
    EXPIRED("Expired subscription"),
    SUSPENDED("Suspended by admin");

    private final String description;

    SubscriptionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE || this == TRIAL;
    }

    public boolean canAccess() {
        return this == ACTIVE || this == TRIAL || this == PAST_DUE;
    }
}

