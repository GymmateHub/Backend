package com.gymmate.shared.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

/**
 * Base entity class with only ID field.
 * Uses PostgreSQL 18's native uuidv7() for time-ordered UUIDs generated server-side.
 * For entities that need audit fields (createdAt, updatedAt, active), extend BaseAuditEntity instead.
 */
@Data
@MappedSuperclass
public abstract class BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ColumnDefault("uuidv7()")
  @Column(
    name = "id",
    updatable = false,
    nullable = false,
    columnDefinition = "UUID DEFAULT uuidv7()"
  )
  private UUID id;
}
