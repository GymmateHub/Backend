package com.gymmate.shared.service;

import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.shared.domain.Organisation;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.infrastructure.OrganisationRepository;
import com.gymmate.user.domain.UserRole;
import com.gymmate.user.domain.UserStatus;
import com.gymmate.user.infrastructure.MemberRepository;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * Service for enforcing organisation subscription limits.
 * Checks maxGyms, maxMembers, maxStaff before allowing new resources to be created.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganisationLimitService {

    private final OrganisationRepository organisationRepository;
    private final GymRepository gymRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    /**
     * Roles that count towards the staff limit.
     * Includes ADMIN, STAFF, and TRAINER but excludes OWNER (who doesn't count against the limit)
     * and MEMBER (who has separate limits).
     */
    private static final Set<UserRole> STAFF_ROLES = Set.of(
        UserRole.ADMIN,
        UserRole.STAFF,
        UserRole.TRAINER
    );

    /**
     * Check if the organisation can create a new gym.
     * @throws DomainException if gym limit is reached
     */
    public void checkCanCreateGym(UUID organisationId) {
        Organisation organisation = getOrganisation(organisationId);

        long currentGymCount = gymRepository.countByOrganisationId(organisationId);
        int maxGyms = organisation.getMaxGyms() != null ? organisation.getMaxGyms() : 1;

        log.debug("Checking gym limit for org {}: current={}, max={}",
            organisationId, currentGymCount, maxGyms);

        if (currentGymCount >= maxGyms) {
            throw new DomainException("GYM_LIMIT_REACHED",
                String.format("Organisation has reached the maximum number of gyms (%d). " +
                    "Please upgrade your subscription to add more locations.", maxGyms));
        }
    }

    /**
     * Check if the organisation can add a new member (across all gyms).
     * @throws DomainException if member limit is reached
     */
    public void checkCanAddMember(UUID organisationId) {
        Organisation organisation = getOrganisation(organisationId);

        long currentMemberCount = memberRepository.countByOrganisationId(organisationId);
        int maxMembers = organisation.getMaxMembers() != null ? organisation.getMaxMembers() : 200;

        log.debug("Checking member limit for org {}: current={}, max={}",
            organisationId, currentMemberCount, maxMembers);

        if (currentMemberCount >= maxMembers) {
            throw new DomainException("MEMBER_LIMIT_REACHED",
                String.format("Organisation has reached the maximum number of members (%d). " +
                    "Please upgrade your subscription to add more members.", maxMembers));
        }
    }

    /**
     * Check if the organisation can add a new staff member (across all gyms).
     * Staff includes users with ADMIN, STAFF, or TRAINER roles.
     * @throws DomainException if staff limit is reached
     */
    public void checkCanAddStaff(UUID organisationId) {
        Organisation organisation = getOrganisation(organisationId);

        long currentStaffCount = countActiveStaff(organisationId);
        int maxStaff = organisation.getMaxStaff() != null ? organisation.getMaxStaff() : 10;

        log.debug("Checking staff limit for org {}: current={}, max={}",
            organisationId, currentStaffCount, maxStaff);

        if (currentStaffCount >= maxStaff) {
            throw new DomainException("STAFF_LIMIT_REACHED",
                String.format("Organisation has reached the maximum number of staff members (%d). " +
                    "Please upgrade your subscription to add more staff.", maxStaff));
        }
    }

    /**
     * Count active staff members (ADMIN, STAFF, TRAINER) in an organisation.
     */
    public long countActiveStaff(UUID organisationId) {
        return userRepository.countByOrganisationIdAndRoleInAndStatus(
            organisationId,
            STAFF_ROLES,
            UserStatus.ACTIVE
        );
    }

    /**
     * Count all staff members (including inactive) in an organisation.
     */
    public long countAllStaff(UUID organisationId) {
        return userRepository.countByOrganisationIdAndRoleIn(organisationId, STAFF_ROLES);
    }

    /**
     * Get current usage stats for an organisation.
     */
    public OrganisationUsage getUsage(UUID organisationId) {
        Organisation organisation = getOrganisation(organisationId);

        long gymCount = gymRepository.countByOrganisationId(organisationId);
        long memberCount = memberRepository.countByOrganisationId(organisationId);
        long staffCount = countActiveStaff(organisationId);

        return new OrganisationUsage(
            gymCount,
            organisation.getMaxGyms() != null ? organisation.getMaxGyms() : 1,
            memberCount,
            organisation.getMaxMembers() != null ? organisation.getMaxMembers() : 200,
            staffCount,
            organisation.getMaxStaff() != null ? organisation.getMaxStaff() : 10
        );
    }

    /**
     * Get organisation by ID or throw exception.
     */
    private Organisation getOrganisation(UUID organisationId) {
        return organisationRepository.findById(organisationId)
            .orElseThrow(() -> new DomainException("ORGANISATION_NOT_FOUND",
                "Organisation not found: " + organisationId));
    }

    /**
     * Record class to hold organisation usage statistics.
     */
    public record OrganisationUsage(
        long currentGyms,
        int maxGyms,
        long currentMembers,
        int maxMembers,
        long currentStaff,
        int maxStaff
    ) {
        public boolean canAddGym() {
            return currentGyms < maxGyms;
        }

        public boolean canAddMember() {
            return currentMembers < maxMembers;
        }

        public boolean canAddStaff() {
            return currentStaff < maxStaff;
        }

        public double gymUsagePercent() {
            return maxGyms > 0 ? (currentGyms * 100.0 / maxGyms) : 0;
        }

        public double memberUsagePercent() {
            return maxMembers > 0 ? (currentMembers * 100.0 / maxMembers) : 0;
        }

        public double staffUsagePercent() {
            return maxStaff > 0 ? (currentStaff * 100.0 / maxStaff) : 0;
        }

        /**
         * Check if any limit is at or above the warning threshold (80%).
         */
        public boolean hasUsageWarning() {
            return gymUsagePercent() >= 80 || memberUsagePercent() >= 80 || staffUsagePercent() >= 80;
        }

        /**
         * Check if any limit is reached (100%).
         */
        public boolean hasLimitReached() {
            return !canAddGym() || !canAddMember() || !canAddStaff();
        }
    }
}

