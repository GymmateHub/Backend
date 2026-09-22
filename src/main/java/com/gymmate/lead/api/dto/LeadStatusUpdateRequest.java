package com.gymmate.lead.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LeadStatusUpdateRequest(
    @NotBlank(message = "Status is required")
    String status
) {}
