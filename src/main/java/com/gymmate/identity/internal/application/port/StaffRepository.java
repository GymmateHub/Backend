package com.gymmate.identity.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.identity.internal.domain.Staff;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Staff entity.
 */
public interface StaffRepository extends DomainRepository<Staff, UUID> {

    // User lookup
    Optional<Staff> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);

    // ========== Organisation-scoped queries (preferred) ==========

    List<Staff> findByOrganisationId(UUID organisationId);

    List<Staff> findAllActiveByOrganisationId(UUID organisationId);

    List<Staff> findByOrganisationIdAndDepartment(UUID organisationId, String department);

    List<Staff> findByOrganisationIdAndPosition(UUID organisationId, String position);

    List<Staff> findByOrganisationIdAndEmploymentType(UUID organisationId, String employmentType);

    // ========== Legacy unscoped queries (use org-scoped variants instead) ==========

    // Department queries
    List<Staff> findByDepartment(String department);

    // Position queries
    List<Staff> findByPosition(String position);

    // Employment type
    List<Staff> findByEmploymentType(String employmentType);

    // Active staff
    List<Staff> findAllActive();
}
