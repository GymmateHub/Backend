package com.gymmate.organisation.domain;

/**
 * Enum representing the subscription status of an organisation.
 */
public enum OrganisationSubscriptionStatus {
    TRIAL("trial"),
    ACTIVE("active"),
    PAST_DUE("past_due"),
    CANCELLED("cancelled"),
    SUSPENDED("suspended"),
    EXPIRED("expired");

    private final String value;

    OrganisationSubscriptionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean isActive() {
        return this == TRIAL || this == ACTIVE;
    }

    public boolean canAccess() {
        return this == TRIAL || this == ACTIVE || this == PAST_DUE;
    }

    public static OrganisationSubscriptionStatus fromValue(String value) {
        for (OrganisationSubscriptionStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown subscription status: " + value);
    }
}

