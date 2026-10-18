package com.gymmate.notification.internal.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for audience preview.
 */
public record AudiencePreviewResponse(
        int totalCount,
        List<RecipientPreview> sampleRecipients
) {

    public record RecipientPreview(UUID memberId, String firstName, String lastName, String email) {
    }
}
