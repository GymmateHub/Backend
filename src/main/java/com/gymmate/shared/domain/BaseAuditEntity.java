package com.gymmate.shared.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Domain entity with audit fields and an active flag. Timestamps are maintained by the
 * persistence layer and written back to the domain object on save.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseAuditEntity extends BaseEntity {

  private String createdBy;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String updatedBy;
  private boolean active = true;

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
