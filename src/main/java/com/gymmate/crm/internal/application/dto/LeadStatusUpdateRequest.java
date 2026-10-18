package com.gymmate.crm.internal.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LeadStatusUpdateRequest(
    @NotBlank(message = "Status is required")
    String status
) {}
