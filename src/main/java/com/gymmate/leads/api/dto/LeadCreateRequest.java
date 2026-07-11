package com.gymmate.leads.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating a lead.
 * gymId is optional — when omitted it is resolved from the tenant context
 * (X-Gym-Id header), so multi-gym tenants can also target a specific gym.
 */
public record LeadCreateRequest(
    UUID gymId,
    @NotBlank(message = "First name is required") @Size(max = 100) String firstName,
    @NotBlank(message = "Last name is required") @Size(max = 100) String lastName,
    @Email(message = "Invalid email format") @Size(max = 255) String email,
    @Size(max = 20) String phone,
    @Size(max = 100) String source,
    String notes,
    UUID assignedTo,
    LocalDate followUpDate) {
}
