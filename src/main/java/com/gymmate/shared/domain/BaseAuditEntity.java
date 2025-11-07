package com.gymmate.shared.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base entity class with audit fields for entities that need tracking.
 * Extends BaseEntity and adds: createdAt, updatedAt, active
 */
@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity extends BaseEntity {

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
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    if (updatedAt == null) {
      updatedAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  protected void preUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // Utility methods for audit management
  public void activate() {
    this.active = true;
  }

  public void deactivate() {
    this.active = false;
  }

  public boolean isActive() {
    return active;
  }
}

