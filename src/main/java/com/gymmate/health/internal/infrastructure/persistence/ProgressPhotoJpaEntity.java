package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.health.internal.domain.ProgressPhoto;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link ProgressPhoto} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "ProgressPhoto")
@Table(name = "progress_photos", indexes = { @Index(name = "idx_photo_member_date", columnList = "member_id,photo_date") })
@Getter
@Setter
@NoArgsConstructor
@DomainModel(ProgressPhoto.class)
public class // Business methods
ProgressPhotoJpaEntity extends GymScopedJpaEntity {

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
    private boolean isPublic = false; // Privacy control - default private
}
