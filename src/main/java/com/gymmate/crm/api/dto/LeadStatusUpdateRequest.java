package com.gymmate.crm.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LeadStatusUpdateRequest(
    @NotBlank(message = "Status is required")
    String status
) {}
