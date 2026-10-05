package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.TenantJpaEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import com.gymmate.identity.internal.domain.Trainer;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link Trainer} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "Trainer")
@Table(name = "trainers")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(Trainer.class)
public class TrainerJpaEntity extends TenantJpaEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // Professional info
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] specializations;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate = BigDecimal.ZERO;

    // Certifications
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String certifications = "[]";

    // Availability
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_availability", columnDefinition = "jsonb")
    private String defaultAvailability;

    // Employment
    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "employment_type", length = 20)
    private String // full_time, part_time, contractor
    employmentType;

    // Status
    @Column(name = "is_accepting_clients")
    private boolean acceptingClients = true;
}
