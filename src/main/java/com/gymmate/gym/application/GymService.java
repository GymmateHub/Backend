package com.gymmate.gym.application;

import com.gymmate.gym.api.dto.GymAnalyticsResponse;
import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.gym.domain.GymStatus;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.user.domain.Member;
import com.gymmate.user.domain.MemberStatus;
import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserStatus;
import com.gymmate.user.infrastructure.MemberRepository;
import com.gymmate.user.infrastructure.UserRepository;
import com.gymmate.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service for gym registration and management use cases.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GymService {

    private final GymRepository gymRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    /**
     * Register a new gym with the system.
     */
    @Transactional
    public Gym registerGym(com.gymmate.gym.api.dto.GymRegistrationRequest request) {
        // Validate that the owner exists and has the correct role
        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.ownerId().toString()));

        if (owner.getRole() != UserRole.ADMIN && owner.getRole() != UserRole.SUPER_ADMIN && owner.getRole() != UserRole.OWNER) {
            throw new DomainException("INVALID_OWNER",
                "Only users with OWNER, ADMIN or SUPER_ADMIN role can register gyms");
        }

        if (!owner.isActive()) {
            throw new DomainException("INACTIVE_OWNER",
                "Owner account must be active to register a gym");
        }

        if (owner.getOrganisationId() == null) {
            throw new DomainException("NO_ORGANISATION",
                "Owner must be associated with an organisation to register a gym");
        }

        // Create gym with required fields
        Gym gym = new Gym(request.name(), request.description(), request.contactEmail(),
                         request.contactPhone(), request.ownerId());

        // Set organisation ID from owner
        gym.setOrganisationId(owner.getOrganisationId());

        // Set optional fields
        if (request.website() != null) {
            gym.setWebsite(request.website());
        }
        if (request.logoUrl() != null) {
            gym.setLogoUrl(request.logoUrl());
        }
        if (request.timezone() != null) {
            gym.setTimezone(request.timezone());
        }
        if (request.currency() != null) {
            gym.setCurrency(request.currency());
        }
        if (request.maxMembers() != null) {
            gym.setMaxMembers(request.maxMembers());
        }

        // Set address if provided
        if (request.street() != null || request.city() != null || request.state() != null ||
            request.postalCode() != null || request.country() != null) {
            gym.updateAddress(request.street(), request.city(), request.state(),
                            request.country(), request.postalCode());
        }

        // Save and return
        return gymRepository.save(gym);
    }

    /**
     * Find a gym by ID.
     */
    public Gym getGymById(UUID id) {
        return gymRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gym", id.toString()));
    }

    /**
     * Update gym address.
     */
    @Transactional
    public Gym updateGymAddress(UUID id, String street, String city, String state,
                              String postalCode, String country) {
        Gym gym = getGymById(id);
        gym.updateAddress(street, city, state, country, postalCode);
        return gymRepository.save(gym);
    }

    /**
     * Update gym details with all fields including address.
     */
    @Transactional
    public Gym updateGymDetails(UUID id, com.gymmate.gym.api.dto.GymUpdateRequest request) {
        Gym gym = getGymById(id);

        // Update basic details
        gym.updateDetails(request.name(), request.description(), request.contactEmail(),
                         request.contactPhone(), request.website());

        // Update address if provided
        if (request.street() != null || request.city() != null || request.state() != null ||
            request.postalCode() != null || request.country() != null) {
            gym.updateAddress(request.street(), request.city(), request.state(),
                            request.country(), request.postalCode());
        }

        // Update other fields
        if (request.logoUrl() != null) {
            gym.setLogoUrl(request.logoUrl());
        }
        if (request.timezone() != null) {
            gym.setTimezone(request.timezone());
        }
        if (request.currency() != null) {
            gym.setCurrency(request.currency());
        }
        if (request.maxMembers() != null) {
            gym.setMaxMembers(request.maxMembers());
        }

        return gymRepository.save(gym);
    }

    /**
     * Find all gyms owned by a specific user.
     */
    public List<Gym> getGymsByOwner(UUID ownerId) {
        return gymRepository.findByOwnerId(ownerId);
    }

    /**
     * Activate a gym.
     */
    @Transactional
    public Gym activateGym(UUID id) {
        Gym gym = getGymById(id);
        gym.activate();
        return gymRepository.save(gym);
    }

    /**
     * Deactivate a gym.
     */
    @Transactional
    public Gym deactivateGym(UUID id) {
        Gym gym = getGymById(id);
        gym.deactivate();
        return gymRepository.save(gym);
    }

    /**
     * Suspend a gym.
     */
    @Transactional
    public Gym suspendGym(UUID id) {
        Gym gym = getGymById(id);
        gym.suspend();
        return gymRepository.save(gym);
    }

    /**
     * Delete a gym.
     */
    @Transactional
    public void deleteGym(UUID id) {
        if (!gymRepository.existsById(id)) {
            throw new ResourceNotFoundException("Gym", id.toString());
        }
        gymRepository.deleteById(id);
    }

    /**
     * Find all active gyms.
     */
    public List<Gym> findActiveGyms() {
        return gymRepository.findByStatus(GymStatus.ACTIVE);
    }

    /**
     * Find gyms by city.
     */
    public List<Gym> findByCity(String city) {
        return gymRepository.findByAddressCity(city);
    }

    /**
     * Find all gyms.
     */
    public List<Gym> findAll() {
        return gymRepository.findAll();
    }

    /**
     * Update gym subscription plan and expiry.
     */
    @Transactional
    public Gym updateSubscription(UUID id, String plan, LocalDateTime expiresAt) {
        Gym gym = getGymById(id);
        gym.updateSubscription(plan, expiresAt);
        return gymRepository.save(gym);
    }

    /**
     * Complete gym onboarding process.
     */
    @Transactional
    public Gym completeOnboarding(UUID id) {
        Gym gym = getGymById(id);
        gym.completeOnboarding();
        return gymRepository.save(gym);
    }

    /**
     * Check if gym subscription is expired.
     */
    public boolean isSubscriptionExpired(UUID id) {
        Gym gym = getGymById(id);
        return gym.isSubscriptionExpired();
    }

    /**
     * Update gym business settings.
     */
    @Transactional
    public Gym updateBusinessSettings(UUID id, String timezone, String currency, String businessHours) {
        Gym gym = getGymById(id);
        if (timezone != null) {
            gym.setTimezone(timezone);
        }
        if (currency != null) {
            gym.setCurrency(currency);
        }
        if (businessHours != null) {
            gym.setBusinessHours(businessHours);
        }
        return gymRepository.save(gym);
    }

    /**
     * Update gym features.
     */
    @Transactional
    public Gym updateFeatures(UUID id, String featuresEnabled) {
        Gym gym = getGymById(id);
        gym.setFeaturesEnabled(featuresEnabled);
        return gymRepository.save(gym);
    }

    /**
     * Update gym max members limit.
     */
    @Transactional
    public Gym updateMaxMembers(UUID id, Integer maxMembers) {
        Gym gym = getGymById(id);
        gym.setMaxMembers(maxMembers);
        return gymRepository.save(gym);
    }

    /**
     * Update gym logo.
     */
    @Transactional
    public Gym updateLogo(UUID id, String logoUrl) {
        Gym gym = getGymById(id);
        gym.setLogoUrl(logoUrl);
        return gymRepository.save(gym);
    }

    /**
     * Update gym website.
     */
    @Transactional
    public Gym updateWebsite(UUID id, String website) {
        Gym gym = getGymById(id);
        gym.setWebsite(website);
        return gymRepository.save(gym);
    }

    /**
     * Get analytics for all gyms owned by a specific user.
     * This is a SaaS multi-tenant method that aggregates data across all gyms owned by the user.
     */
    public GymAnalyticsResponse getOwnerAnalytics(UUID ownerId) {
        // Validate owner exists
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", ownerId.toString()));

        // Get all gyms owned by this user
        List<Gym> ownedGyms = gymRepository.findByOwnerId(ownerId);

        // Calculate total gyms
        int totalGyms = ownedGyms.size();

        // Calculate active locations (gyms with ACTIVE status)
        long activeLocations = ownedGyms.stream()
                .filter(gym -> gym.getStatus() == GymStatus.ACTIVE)
                .count();

        // Calculate total capacity across all gyms
        int totalCapacity = ownedGyms.stream()
                .mapToInt(gym -> gym.getMaxMembers() != null ? gym.getMaxMembers() : 0)
                .sum();

        // Calculate total members, staff, and trainers across all gyms
        int totalMembers = 0;
        int totalActiveMembers = 0;
        int totalStaff = 0;
        int totalTrainers = 0;

        for (Gym gym : ownedGyms) {
            // Count members from members table (not users table)
            totalMembers += memberRepository.countByGymId(gym.getId());
            totalActiveMembers += memberRepository.countByGymIdAndStatus(gym.getId(), MemberStatus.ACTIVE);

            // Staff and trainers are in the organisation, not specific to a gym
            // For now, count them from the organisation
        }

        // Count staff and trainers at organisation level
        if (owner.getOrganisationId() != null) {
            totalStaff = (int) userRepository.countByOrganisationIdAndRole(
                owner.getOrganisationId(), UserRole.STAFF);
            totalTrainers = (int) userRepository.countByOrganisationIdAndRole(
                owner.getOrganisationId(), UserRole.TRAINER);
        }

        // Calculate average utilization across all gyms
        double avgUtilization = 0.0;
        if (totalCapacity > 0) {
            avgUtilization = (totalMembers * 100.0) / totalCapacity;
        }

        // TODO: Calculate total revenue this month (requires payment/subscription integration)
        BigDecimal totalRevenue = BigDecimal.ZERO;

        return GymAnalyticsResponse.builder()
                .totalGyms(totalGyms)
                .activeLocations((int) activeLocations)
                .totalCapacity(totalCapacity)
                .avgUtilization(Math.round(avgUtilization * 100.0) / 100.0) // Round to 2 decimal places
                .totalRevenue(totalRevenue)
                .totalMembers(totalMembers)
                .totalActiveMembers(totalActiveMembers)
                .totalStaff(totalStaff)
                .totalTrainers(totalTrainers)
                .build();
    }

    /**
     * Get analytics for a specific gym owned by a user.
     * Validates that the gym belongs to the owner before returning analytics.
     */
    public GymAnalyticsResponse getGymAnalytics(UUID gymId, UUID ownerId) {
        // Get gym and verify ownership
        Gym gym = getGymById(gymId);

        if (!gym.getOwnerId().equals(ownerId)) {
            throw new DomainException("ACCESS_DENIED",
                "You do not have permission to view analytics for this gym");
        }

        // Calculate gym-specific metrics
        long currentMembers = memberRepository.countByGymId(gymId);
        long activeMembers = memberRepository.countByGymIdAndStatus(gymId, MemberStatus.ACTIVE);

        // Staff and trainers are organisation-level, not gym-specific
        long staff = 0;
        long trainers = 0;
        if (gym.getOrganisationId() != null) {
            staff = userRepository.countByOrganisationIdAndRole(gym.getOrganisationId(), UserRole.STAFF);
            trainers = userRepository.countByOrganisationIdAndRole(gym.getOrganisationId(), UserRole.TRAINER);
        }

        int maxMembers = gym.getMaxMembers() != null ? gym.getMaxMembers() : 0;
        double utilization = 0.0;
        if (maxMembers > 0) {
            utilization = (currentMembers * 100.0) / maxMembers;
        }

        // TODO: Calculate revenue for this gym this month
        BigDecimal gymRevenue = BigDecimal.ZERO;

        return GymAnalyticsResponse.builder()
                .gymId(gymId)
                .gymName(gym.getName())
                .totalGyms(1)
                .activeLocations(gym.getStatus() == GymStatus.ACTIVE ? 1 : 0)
                .totalCapacity(maxMembers)
                .avgUtilization(Math.round(utilization * 100.0) / 100.0)
                .totalRevenue(gymRevenue)
                .totalMembers((int) currentMembers)
                .totalActiveMembers((int) activeMembers)
                .totalStaff((int) staff)
                .totalTrainers((int) trainers)
                .currentMembers((int) currentMembers)
                .maxMembers(maxMembers)
                .utilizationPercentage(Math.round(utilization * 100.0) / 100.0)
                .build();
    }
}
