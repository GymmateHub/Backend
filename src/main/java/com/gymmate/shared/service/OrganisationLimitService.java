package com.gymmate.shared.service;

import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.shared.domain.Organisation;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.infrastructure.OrganisationRepository;
import com.gymmate.user.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Check if the organisation can create a new gym.
     * @throws DomainException if gym limit is reached
     */
    public void checkCanCreateGym(UUID organisationId) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new DomainException("ORGANISATION_NOT_FOUND",
                    "Organisation not found: " + organisationId));

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
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new DomainException("ORGANISATION_NOT_FOUND",
                    "Organisation not found: " + organisationId));

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
     * @throws DomainException if staff limit is reached
     */
    public void checkCanAddStaff(UUID organisationId) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new DomainException("ORGANISATION_NOT_FOUND",
                    "Organisation not found: " + organisationId));

        // Count users with STAFF, TRAINER, or ADMIN roles in this organisation
        // Note: This would need a proper query in UserRepository
        // For now, we'll use maxStaff from organisation
        int maxStaff = organisation.getMaxStaff() != null ? organisation.getMaxStaff() : 10;

        log.debug("Staff limit check for org {}: max={}", organisationId, maxStaff);

        // TODO: Implement actual staff count when UserRepository is updated
    }

    /**
     * Get current usage stats for an organisation.
     */
    public OrganisationUsage getUsage(UUID organisationId) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new DomainException("ORGANISATION_NOT_FOUND",
                    "Organisation not found: " + organisationId));

        long gymCount = gymRepository.countByOrganisationId(organisationId);
        long memberCount = memberRepository.countByOrganisationId(organisationId);

        return new OrganisationUsage(
            gymCount,
            organisation.getMaxGyms() != null ? organisation.getMaxGyms() : 1,
            memberCount,
            organisation.getMaxMembers() != null ? organisation.getMaxMembers() : 200,
            0, // TODO: Implement staff count
            organisation.getMaxStaff() != null ? organisation.getMaxStaff() : 10
        );
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
    }
}

