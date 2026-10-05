package com.gymmate.shared.domain;

import lombok.Data;

import java.util.UUID;

/**
 * Root of every domain entity: identity only. Pure Java (no persistence mapping) —
 * the JPA representation lives in
 * {@code com.gymmate.shared.infrastructure.persistence.BaseJpaEntity}; ids are assigned by
 * the database (PostgreSQL {@code uuidv7()}) and written back on save.
 */
@Data
public abstract class BaseEntity {

  private UUID id;
}
