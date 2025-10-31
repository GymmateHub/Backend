package com.gymmate.shared.domain;

import com.gymmate.shared.multitenancy.TenantContext;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class TenantEntity extends BaseEntity {
  @Column(name = "gym_id", nullable = false)
  private UUID gymId;

  @Override
  protected void prePersist() {
    super.prePersist();

    if (gymId == null) {
      gymId = TenantContext.requireCurrentTenantId();
    }
  }
}
