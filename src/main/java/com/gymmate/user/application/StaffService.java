package com.gymmate.user.application;

import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.user.domain.Staff;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.StaffRepository;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Application service for staff management use cases.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffService {

    private final StaffRepository staffRepository;
    private final UserRepository userRepository;

    /**
     * Create a new staff profile for an existing user.
     */
    @Transactional
    public Staff createStaff(UUID userId, String position, String department,
                            BigDecimal hourlyWage, LocalDate hireDate, String employmentType) {
        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        // Check if staff profile already exists
        if (staffRepository.existsByUserId(userId)) {
            throw new DomainException("STAFF_ALREADY_EXISTS",
                    "Staff profile already exists for user: " + userId);
        }

        // Create staff
        Staff staff = Staff.builder()
                .userId(userId)
                .position(position)
                .department(department)
                .hourlyWage(hourlyWage)
                .hireDate(hireDate != null ? hireDate : LocalDate.now())
                .employmentType(employmentType)
                .permissions("[]")
                .build();

        // Set organisationId from linked user
        if (user.getOrganisationId() != null) {
            staff.setOrganisationId(user.getOrganisationId());
        }

        return staffRepository.save(staff);
    }

    /**
     * Find staff by ID.
     */
    public Staff findById(UUID id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id.toString()));
    }

    /**
     * Find staff by user ID.
     */
    public Staff findByUserId(UUID userId) {
        return staffRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", "userId=" + userId));
    }

    /**
     * Update staff position.
     */
    @Transactional
    public Staff updatePosition(UUID staffId, String position, String department) {
        Staff staff = findById(staffId);
        staff.updatePosition(position, department);
        return staffRepository.save(staff);
    }

    /**
     * Update staff wage.
     */
    @Transactional
    public Staff updateWage(UUID staffId, BigDecimal hourlyWage) {
        Staff staff = findById(staffId);
        staff.updateWage(hourlyWage);
        return staffRepository.save(staff);
    }

    /**
     * Update staff schedule.
     */
    @Transactional
    public Staff updateSchedule(UUID staffId, String scheduleJson) {
        Staff staff = findById(staffId);
        staff.updateSchedule(scheduleJson);
        return staffRepository.save(staff);
    }

    /**
     * Update staff permissions.
     */
    @Transactional
    public Staff updatePermissions(UUID staffId, String permissionsJson) {
        Staff staff = findById(staffId);
        staff.setPermissions(permissionsJson);
        return staffRepository.save(staff);
    }

    /**
     * Find staff by department within organisation.
     */
    public List<Staff> findByDepartment(UUID organisationId, String department) {
        return staffRepository.findByOrganisationIdAndDepartment(organisationId, department);
    }

    /**
     * Find staff by position within organisation.
     */
    public List<Staff> findByPosition(UUID organisationId, String position) {
        return staffRepository.findByOrganisationIdAndPosition(organisationId, position);
    }

    /**
     * Find staff by employment type within organisation.
     */
    public List<Staff> findByEmploymentType(UUID organisationId, String employmentType) {
        return staffRepository.findByOrganisationIdAndEmploymentType(organisationId, employmentType);
    }

    /**
     * Find all active staff within organisation.
     */
    public List<Staff> findAllActive(UUID organisationId) {
        return staffRepository.findAllActiveByOrganisationId(organisationId);
    }

    /**
     * Find all staff within organisation.
     */
    public List<Staff> findAllByOrganisation(UUID organisationId) {
        return staffRepository.findByOrganisationId(organisationId);
    }

    /**
     * @deprecated Use {@link #findAllByOrganisation(UUID)} instead.
     */
    @Deprecated
    public List<Staff> findAll() {
        return staffRepository.findAll();
    }

    /**
     * @deprecated Use {@link #findAllActive(UUID)} instead.
     */
    @Deprecated
    public List<Staff> findAllActive() {
        return staffRepository.findAllActive();
    }

    /**
     * @deprecated Use {@link #findByDepartment(UUID, String)} instead.
     */
    @Deprecated
    public List<Staff> findByDepartment(String department) {
        return staffRepository.findByDepartment(department);
    }

    /**
     * @deprecated Use {@link #findByPosition(UUID, String)} instead.
     */
    @Deprecated
    public List<Staff> findByPosition(String position) {
        return staffRepository.findByPosition(position);
    }

    /**
     * @deprecated Use {@link #findByEmploymentType(UUID, String)} instead.
     */
    @Deprecated
    public List<Staff> findByEmploymentType(String employmentType) {
        return staffRepository.findByEmploymentType(employmentType);
    }

    /**
     * Deactivate staff.
     */
    @Transactional
    public Staff deactivate(UUID staffId) {
        Staff staff = findById(staffId);
        staff.setActive(false);
        return staffRepository.save(staff);
    }

    /**
     * Activate staff.
     */
    @Transactional
    public Staff activate(UUID staffId) {
        Staff staff = findById(staffId);
        staff.setActive(true);
        return staffRepository.save(staff);
    }
}

