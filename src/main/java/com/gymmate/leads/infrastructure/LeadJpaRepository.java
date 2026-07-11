package com.gymmate.leads.infrastructure;

import com.gymmate.leads.domain.Lead;
import com.gymmate.leads.domain.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for Lead entity.
 */
@Repository
public interface LeadJpaRepository extends JpaRepository<Lead, UUID> {

  @Query("SELECT l FROM Lead l WHERE l.gymId = :gymId AND l.active = true ORDER BY l.createdAt DESC")
  List<Lead> findByGymId(@Param("gymId") UUID gymId);

  @Query("SELECT l FROM Lead l WHERE l.organisationId = :organisationId AND l.active = true ORDER BY l.createdAt DESC")
  List<Lead> findByOrganisationId(@Param("organisationId") UUID organisationId);

  @Query("SELECT l FROM Lead l WHERE l.gymId = :gymId AND l.status = :status AND l.active = true ORDER BY l.createdAt DESC")
  List<Lead> findByGymIdAndStatus(@Param("gymId") UUID gymId, @Param("status") LeadStatus status);

  @Query("SELECT l FROM Lead l WHERE l.gymId = :gymId AND LOWER(l.email) = LOWER(:email) AND l.active = true")
  Optional<Lead> findByGymIdAndEmail(@Param("gymId") UUID gymId, @Param("email") String email);

  @Query("SELECT COUNT(l) FROM Lead l WHERE l.gymId = :gymId AND l.status = :status AND l.active = true")
  long countByGymIdAndStatus(@Param("gymId") UUID gymId, @Param("status") LeadStatus status);
}
