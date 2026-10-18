package com.gymmate.scheduling.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.*;
import com.gymmate.scheduling.internal.domain.GymArea;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link GymArea} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "GymArea")
@Table(name = "gym_areas")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(GymArea.class)
public class GymAreaJpaEntity extends GymScopedJpaEntity {

    // Note: gymId is inherited from GymScopedJpaEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "area_type", length = 50)
    private String areaType; // studio, pool, main_floor, outdoor, virtual

    @Column
    private Integer capacity;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] amenities;

    // Booking rules
    @Column(name = "requires_booking")
    private boolean requiresBooking = false;

    @Column(name = "advance_booking_hours")
    private Integer advanceBookingHours = 24;
}
