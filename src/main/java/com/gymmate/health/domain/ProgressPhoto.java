package com.gymmate.health.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
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
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "progress_photos", indexes = {
    @Index(name = "idx_photo_member_date", columnList = "member_id,photo_date")
})
public class ProgressPhoto extends GymScopedEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "photo_date", nullable = false)
    private LocalDateTime photoDate;

    @Column(name = "photo_url", length = 500)
    private String photoUrl; // Will be populated when file upload is implemented

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "weight_at_time", precision = 10, scale = 2)
    private BigDecimal weightAtTime; // Record weight when photo was taken

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_public")
    @Builder.Default
    private boolean isPublic = false; // Privacy control - default private

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
