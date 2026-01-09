package com.gymmate.health.api.dto;

import com.gymmate.health.application.HealthMetricService;

/**
 * Response DTO for body composition snapshot.
 */
public record BodyCompositionResponse(
    HealthMetricResponse weight,
    HealthMetricResponse bodyFat,
    HealthMetricResponse muscleMass,
    HealthMetricResponse bmi,
    HealthMetricResponse waistCircumference
) {
    public static BodyCompositionResponse from(HealthMetricService.BodyCompositionSnapshot snapshot) {
        return new BodyCompositionResponse(
            snapshot.weight() != null ? HealthMetricResponse.from(snapshot.weight()) : null,
            snapshot.bodyFat() != null ? HealthMetricResponse.from(snapshot.bodyFat()) : null,
            snapshot.muscleMass() != null ? HealthMetricResponse.from(snapshot.muscleMass()) : null,
            snapshot.bmi() != null ? HealthMetricResponse.from(snapshot.bmi()) : null,
            snapshot.waistCircumference() != null ? HealthMetricResponse.from(snapshot.waistCircumference()) : null
        );
    }
}
