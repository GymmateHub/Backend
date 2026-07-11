package com.gymmate.leads.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for updating a lead's status.
 * Accepts the status as a string (case-insensitive) so UI clients
 * can send either "contacted" or "CONTACTED".
 */
public record UpdateLeadStatusRequest(
    @NotBlank(message = "Status is required") String status) {
}
