package com.gymmate.user.domain;

/**
 * Enumeration of user roles in the system.
 */
public enum UserRole {
    /**
     * Regular gym member who can book classes, view schedules, etc.
     */
    MEMBER,
    
    /**
     * Gym trainer/instructor who can conduct classes
     */
    TRAINER,
    
    /**
     * Gym owner/manager who can manage the gym operations
     */
    GYM_OWNER,
    
    /**
     * System administrator with full access
     */
    ADMIN
}