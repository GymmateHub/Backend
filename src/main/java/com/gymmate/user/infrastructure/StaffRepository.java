package com.gymmate.user.infrastructure;

import com.gymmate.user.domain.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Staff entity.
 */
@Repository
public interface StaffRepository extends JpaRepository<Staff, UUID> {

    // User lookup
    Optional<Staff> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);

    // Department queries
    List<Staff> findByDepartment(String department);

    // Position queries
    List<Staff> findByPosition(String position);

    // Employment type
    List<Staff> findByEmploymentType(String employmentType);

    // Active staff
    @Query("SELECT s FROM Staff s WHERE s.active = true")
    List<Staff> findAllActive();
}

