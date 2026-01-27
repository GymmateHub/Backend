package com.gymmate.notification.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for audience preview.
 */
@Data
@Builder
public class AudiencePreviewResponse {

    private int totalCount;
    private List<RecipientPreview> sampleRecipients;

    @Data
    @Builder
    public static class RecipientPreview {
        private UUID memberId;
        private String firstName;
        private String lastName;
        private String email;
    }
}
