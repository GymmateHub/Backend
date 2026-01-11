package com.gymmate.shared.domain;

import jakarta.persistence.*;
import lombok.Data;
//import org.hibernate.annotations.Generated;
//import org.hibernate.annotations.GenerationTime;

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
//  @Generated(GenerationTime.INSERT)
//  @Column(
//    name = "id",
//    updatable = false,
//    nullable = false,
//    columnDefinition = "UUID DEFAULT uuidv7()"
//  )
  private UUID id;
}
