package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.constants.MemberStatus;
import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;
import com.gymmate.identity.internal.domain.Member;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link Member} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "Member")
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(Member.class)
public class MemberJpaEntity extends GymScopedJpaEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // Note: gymId is inherited from GymScopedJpaEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
    @Column(name = "membership_number", unique = true, length = 50)
    private String membershipNumber;

    @Column(name = "join_date")
    private LocalDate joinDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;

    // Emergency contact
    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_relationship", length = 50)
    private String emergencyContactRelationship;

    // Health information
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "medical_conditions", columnDefinition = "text[]")
    private String[] medicalConditions;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] allergies;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] medications;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "fitness_goals", columnDefinition = "text[]")
    private String[] fitnessGoals;

    @Column(name = "experience_level", length = 20)
    private String experienceLevel; // beginner, intermediate, advanced

    // Preferences
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_workout_times", columnDefinition = "jsonb")
    private String preferredWorkoutTimes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "communication_preferences", columnDefinition = "jsonb")
    private String communicationPreferences;

    // Waiver & agreements
    @Column(name = "waiver_signed")
    private boolean waiverSigned = false;

    @Column(name = "waiver_signed_date")
    private LocalDate waiverSignedDate;

    @Column(name = "photo_consent")
    private boolean photoConsent = false;
}
