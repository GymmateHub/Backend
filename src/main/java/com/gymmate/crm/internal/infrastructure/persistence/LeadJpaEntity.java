package com.gymmate.crm.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.crm.internal.domain.Lead;
import com.gymmate.crm.internal.domain.LeadStatus;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link Lead} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "Lead")
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(Lead.class)
public class LeadJpaEntity extends GymScopedJpaEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 100)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LeadStatus status = LeadStatus.NEW;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    @Column(name = "converted_member_id")
    private UUID convertedMemberId;
}
