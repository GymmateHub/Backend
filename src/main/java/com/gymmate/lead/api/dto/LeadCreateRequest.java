package com.gymmate.lead.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

public record LeadCreateRequest(
    UUID gymId,
    @NotBlank(message = "First name is required")
    String firstName,
    @NotBlank(message = "Last name is required")
    String lastName,
    String email,
    String phone,
    String source,
    String notes,
    UUID assignedTo,
    LocalDate followUpDate
) {}
