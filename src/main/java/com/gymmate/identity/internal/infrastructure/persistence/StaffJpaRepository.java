package com.gymmate.identity.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Staff entity.
 */
@Repository
public interface StaffJpaRepository extends JpaRepository<StaffJpaEntity, UUID> {

    // User lookup
    Optional<StaffJpaEntity> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    // ========== Organisation-scoped queries (preferred) ==========
    List<StaffJpaEntity> findByOrganisationId(UUID organisationId);

    @Query("SELECT s FROM Staff s WHERE s.organisationId = :organisationId AND s.active = true")
    List<StaffJpaEntity> findAllActiveByOrganisationId(@Param("organisationId") UUID organisationId);

    List<StaffJpaEntity> findByOrganisationIdAndDepartment(UUID organisationId, String department);

    List<StaffJpaEntity> findByOrganisationIdAndPosition(UUID organisationId, String position);

    List<StaffJpaEntity> findByOrganisationIdAndEmploymentType(UUID organisationId, String employmentType);

    // ========== Legacy unscoped queries (use org-scoped variants instead) ==========
    // Department queries
    List<StaffJpaEntity> findByDepartment(String department);

    // Position queries
    List<StaffJpaEntity> findByPosition(String position);

    // Employment type
    List<StaffJpaEntity> findByEmploymentType(String employmentType);

    // Active staff
    @Query("SELECT s FROM Staff s WHERE s.active = true")
    List<StaffJpaEntity> findAllActive();
}
