package com.gymmate.membership.domain;

/**
 * Enumeration of gym statuses in the system.
 */
public enum GymStatus {
    /**
     * Gym is active and accepting members
     */
    ACTIVE,
    
    /**
     * Gym is temporarily inactive
     */
    INACTIVE,
    
    /**
     * Gym is suspended due to policy violations or other issues
     */
    SUSPENDED
}