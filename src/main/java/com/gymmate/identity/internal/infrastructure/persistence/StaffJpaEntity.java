package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.TenantJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import com.gymmate.identity.internal.domain.Staff;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link Staff} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "Staff")
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(Staff.class)
public class StaffJpaEntity extends TenantJpaEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // Job details
    @Column(length = 100)
    private String position;

    @Column(length = 50)
    private String // front_desk, maintenance, management, cleaning
    department;

    @Column(name = "hourly_wage", precision = 10, scale = 2)
    private BigDecimal hourlyWage;

    // Employment
    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "employment_type", length = 20)
    private String // full_time, part_time, contractor
    employmentType;

    // Schedule
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_schedule", columnDefinition = "jsonb")
    private String defaultSchedule;

    // Permissions
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String // ["access_control", "pos", "member_management"]
    permissions = "[]";
}
