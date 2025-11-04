package com.gymmate.shared.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

/**
 * Base entity class with only ID field.
 * For entities that need audit fields (createdAt, updatedAt, active), extend BaseAuditEntity instead.
 */
@Data
@MappedSuperclass
public abstract class BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
}
