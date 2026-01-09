package com.gymmate.health.domain;

/**
 * Enum representing the status of a fitness goal.
 */
public enum GoalStatus {
    ACTIVE,       // Currently working towards goal
    ACHIEVED,     // Goal successfully reached
    ABANDONED,    // Goal given up or cancelled
    ON_HOLD       // Goal temporarily paused
}
