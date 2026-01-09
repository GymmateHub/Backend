package com.gymmate.health.domain;

/**
 * Enum representing types of health metrics that can be tracked.
 * Used for body composition, vital signs, and fitness measurements.
 */
public enum MetricType {
    // Body Composition
    WEIGHT,
    HEIGHT,
    BODY_FAT_PERCENTAGE,
    MUSCLE_MASS,
    BMI,
    BODY_MASS_INDEX,

    // Body Measurements
    WAIST_CIRCUMFERENCE,
    CHEST_CIRCUMFERENCE,
    HIP_CIRCUMFERENCE,
    BICEP_CIRCUMFERENCE,
    THIGH_CIRCUMFERENCE,
    NECK_CIRCUMFERENCE,
    CALF_CIRCUMFERENCE,

    // Vital Signs
    BLOOD_PRESSURE_SYSTOLIC,
    BLOOD_PRESSURE_DIASTOLIC,
    RESTING_HEART_RATE,
    MAX_HEART_RATE,

    // Fitness Metrics
    VO2_MAX,
    ONE_REP_MAX_BENCH,
    ONE_REP_MAX_SQUAT,
    ONE_REP_MAX_DEADLIFT
}
