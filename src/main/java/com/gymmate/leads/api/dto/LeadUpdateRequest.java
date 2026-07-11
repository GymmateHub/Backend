package com.gymmate.leads.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for updating a lead. All fields are optional (partial update).
 */
public record LeadUpdateRequest(
    @Size(max = 100) String firstName,
    @Size(max = 100) String lastName,
    @Email(message = "Invalid email format") @Size(max = 255) String email,
    @Size(max = 20) String phone,
    @Size(max = 100) String source,
    String notes,
    UUID assignedTo,
    LocalDate followUpDate) {
}
