package com.gymmate.crm.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Lead extends GymScopedEntity {

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String source;

    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    private String notes;

    private UUID assignedTo;

    private LocalDate followUpDate;

    private LocalDateTime convertedAt;

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
