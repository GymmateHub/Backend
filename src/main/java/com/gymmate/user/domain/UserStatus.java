package com.gymmate.user.domain;

/**
 * Enumeration of user statuses in the system.
 */
public enum UserStatus {
    /**
     * User account is active and can access the system
     */
    ACTIVE,
    
    /**
     * User account is temporarily inactive
     */
    INACTIVE,
    
    /**
     * User account is suspended due to policy violations
     */
    SUSPENDED
}