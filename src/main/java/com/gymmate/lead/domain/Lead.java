package com.gymmate.lead.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Lead extends GymScopedEntity {

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
    @Builder.Default
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

    public void updateDetails(String firstName, String lastName, String email, String phone,
                              String source, String notes, UUID assignedTo, LocalDate followUpDate) {
        if (firstName != null && !firstName.isBlank()) this.firstName = firstName;
        if (lastName != null && !lastName.isBlank()) this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.source = source;
        this.notes = notes;
        this.assignedTo = assignedTo;
        this.followUpDate = followUpDate;
    }

    public void updateStatus(LeadStatus newStatus) {
        this.status = newStatus;
        if (newStatus == LeadStatus.CONVERTED && this.convertedAt == null) {
            this.convertedAt = LocalDateTime.now();
        }
    }

    public void convert(UUID memberId) {
        this.status = LeadStatus.CONVERTED;
        this.convertedAt = LocalDateTime.now();
        this.convertedMemberId = memberId;
    }
}
