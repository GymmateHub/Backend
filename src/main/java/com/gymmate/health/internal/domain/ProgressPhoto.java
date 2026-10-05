package com.gymmate.health.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ProgressPhoto entity for tracking member progress photos over time.
 * Entity structure only - file upload implementation deferred.
 * Implements FR-014: Progress Photos (structure).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class ProgressPhoto extends GymScopedEntity {

    private UUID memberId;

    private LocalDateTime photoDate;

    private String photoUrl; // Will be populated when file upload is implemented

    private String thumbnailUrl;

    private BigDecimal weightAtTime; // Record weight when photo was taken

    private String notes;

    @Builder.Default
    private 
    boolean isPublic = false; // Privacy control - default private

    // Business methods

    public void makePublic() {
        this.isPublic = true;
    }

    public void makePrivate() {
        this.isPublic = false;
    }

    public boolean hasPhoto() {
        return photoUrl != null && !photoUrl.isBlank();
    }
}
