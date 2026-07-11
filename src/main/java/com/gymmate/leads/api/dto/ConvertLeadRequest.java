package com.gymmate.leads.api.dto;

import java.util.UUID;

/**
 * Request DTO for converting a lead. memberId optionally links the
 * newly created member record to the lead for attribution.
 */
public record ConvertLeadRequest(
    UUID memberId) {
}
