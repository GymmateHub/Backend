package com.gymmate.shared.domain;

import com.gymmate.shared.multitenancy.TenantContext;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base entity class with common fields for all entities.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "gym_id", nullable = false)
  private UUID gymId;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "is_active")
  private boolean active = true;

  @PrePersist
  protected void prePersist() {
    if (gymId == null) {
      gymId = TenantContext.getCurrentTenantId();
    }
  }
}
