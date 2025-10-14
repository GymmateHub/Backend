package com.gymmate.Gym.application;

import com.gymmate.Gym.domain.Address;
import com.gymmate.Gym.domain.Gym;
import com.gymmate.Gym.domain.GymRepository;
import com.gymmate.Gym.domain.GymStatus;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.UserRepository;
import com.gymmate.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                           String street, String city, String state, String postalCode, String country,
                           String contactEmail, String contactPhone) {

        // Validate that the owner exists and has the correct role
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", ownerId.toString()));

        if (owner.getRole() != UserRole.GYM_OWNER && owner.getRole() != UserRole.GYM_ADMIN) {
            throw new DomainException("INVALID_GYM_OWNER",
                "Only users with GYM_OWNER or ADMIN role can register gyms");
        }

        if (!owner.isActive()) {
            throw new DomainException("INACTIVE_OWNER",
                "Owner account must be active to register a gym");
        }

        // Create address
        Address address = new Address(street, city, state, postalCode, country);

        // Create gym
        Gym gym = new Gym(name, description, address, contactEmail, contactPhone, ownerId);

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
     * Update gym details.
     */
    @Transactional
    public Gym updateGymDetails(UUID id, String name, String description,
                               String street, String city, String state, String postalCode, String country,
                               String contactEmail, String contactPhone) {

        Gym gym = getGymById(id);
        Address address = new Address(street, city, state, postalCode, country);

        gym.updateDetails(name, description, address, contactEmail, contactPhone);
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
}
