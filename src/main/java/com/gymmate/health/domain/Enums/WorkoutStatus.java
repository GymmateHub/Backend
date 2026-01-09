package com.gymmate.health.domain.Enums;

/**
 * Enum representing workout completion status.
 */
public enum WorkoutStatus {
    PLANNED,       // Scheduled but not started
    IN_PROGRESS,   // Currently being performed
    COMPLETED,     // Finished successfully
    SKIPPED        // Planned but not performed
}
