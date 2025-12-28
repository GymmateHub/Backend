package com.gymmate.gym.application;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.gym.domain.GymStatus;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.UserRepository;
import com.gymmate.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Register a new gym with the system.
     */
    @Transactional
    public Gym registerGym(UUID ownerId, String name, String description,
                          String contactEmail, String contactPhone) {

        // Validate that the owner exists and has the correct role
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", ownerId.toString()));

        if (owner.getRole() != UserRole.ADMIN && owner.getRole() != UserRole.SUPER_ADMIN && owner.getRole() != UserRole.OWNER) {
            throw new DomainException("INVALID_OWNER",
                "Only users with OWNER, ADMIN or SUPER_ADMIN role can register gyms");
        }

        if (!owner.isActive()) {
            throw new DomainException("INACTIVE_OWNER",
                "Owner account must be active to register a gym");
        }

        // Create gym with required fields
        Gym gym = new Gym(name, description, contactEmail, contactPhone, ownerId);

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
     * Update gym details without address.
     */
    @Transactional
    public Gym updateGymDetails(UUID id, String name, String description,
                              String contactEmail, String contactPhone) {
        Gym gym = getGymById(id);
        gym.updateDetails(name, description, contactEmail, contactPhone, null);
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
}
