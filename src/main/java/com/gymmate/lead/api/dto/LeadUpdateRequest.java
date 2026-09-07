package com.gymmate.lead.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public record LeadUpdateRequest(
    String firstName,
    String lastName,
    String email,
    String phone,
    String source,
    String notes,
    UUID assignedTo,
    LocalDate followUpDate
) {}
