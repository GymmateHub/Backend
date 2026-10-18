package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.health.internal.domain.enums.MetricType;
import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.health.internal.domain.HealthMetric;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link HealthMetric} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "HealthMetric")
@Table(name = "health_metrics", indexes = { @Index(name = "idx_metric_member_type_date", columnList = "member_id,metric_type,measurement_date"), @Index(name = "idx_metric_gym_date", columnList = "gym_id,measurement_date") })
@Getter
@Setter
@NoArgsConstructor
@DomainModel(HealthMetric.class)
public class // Business methods
HealthMetricJpaEntity extends GymScopedJpaEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "measurement_date", nullable = false)
    private LocalDateTime measurementDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 50)
    private MetricType metricType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(nullable = false, length = 10)
    private String unit; // kg, lbs, %, cm, bpm, etc.

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recorded_by_user_id")
    private UUID recordedByUserId;
}
