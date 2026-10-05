package com.gymmate.identity.internal.application.port;

import com.gymmate.identity.internal.domain.Staff;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Spring Data JPA repository for Staff entity.
 */
public interface StaffRepository {

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

Staff save(Staff entity);

List<Staff> saveAll(Iterable<Staff> entities);

Optional<Staff> findById(UUID id);

boolean existsById(UUID id);

List<Staff> findAll();

List<Staff> findAllById(Iterable<UUID> ids);

long count();

void deleteById(UUID id);

void delete(Staff entity);

void deleteAll(Iterable<Staff> entities);

Staff saveAndFlush(Staff entity);

void flush();

Page<Staff> findAll(Pageable pageable);
}
