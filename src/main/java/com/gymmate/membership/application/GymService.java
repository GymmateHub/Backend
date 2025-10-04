package com.gymmate.membership.application;

import com.gymmate.membership.domain.Address;
import com.gymmate.membership.domain.Gym;
import com.gymmate.membership.domain.GymRepository;
import com.gymmate.membership.domain.GymStatus;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRepository;
import com.gymmate.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public Gym registerGym(Long ownerId, String name, String description, 
                          String street, String city, String state, String postalCode, String country,
                          String contactEmail, String contactPhone) {
        
        // Validate that the owner exists and has the correct role
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", ownerId.toString()));
        
        if (owner.getRole() != UserRole.GYM_OWNER && owner.getRole() != UserRole.ADMIN) {
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
    public Gym findById(Long id) {
        return gymRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gym", id.toString()));
    }
    
    /**
     * Update gym details.
     */
    @Transactional
    public Gym updateGymDetails(Long gymId, String name, String description,
                               String street, String city, String state, String postalCode, String country,
                               String contactEmail, String contactPhone) {
        
        Gym gym = findById(gymId);
        Address address = new Address(street, city, state, postalCode, country);
        
        gym.updateDetails(name, description, address, contactEmail, contactPhone);
        return gymRepository.save(gym);
    }
    
    /**
     * Find all gyms owned by a specific user.
     */
    public List<Gym> findByOwnerId(Long ownerId) {
        return gymRepository.findByOwnerId(ownerId);
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
     * Activate a gym.
     */
    @Transactional
    public Gym activateGym(Long gymId) {
        Gym gym = findById(gymId);
        gym.activate();
        return gymRepository.save(gym);
    }
    
    /**
     * Deactivate a gym.
     */
    @Transactional
    public Gym deactivateGym(Long gymId) {
        Gym gym = findById(gymId);
        gym.deactivate();
        return gymRepository.save(gym);
    }
    
    /**
     * Suspend a gym.
     */
    @Transactional
    public Gym suspendGym(Long gymId) {
        Gym gym = findById(gymId);
        gym.suspend();
        return gymRepository.save(gym);
    }
    
    /**
     * Find all gyms.
     */
    public List<Gym> findAll() {
        return gymRepository.findAll();
    }
    
    /**
     * Validate gym ownership for operations.
     */
    public void validateOwnership(Long gymId, Long userId) {
        Gym gym = findById(gymId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        
        // Allow gym owner or admin to perform operations
        if (!gym.getOwnerId().equals(userId) && user.getRole() != UserRole.ADMIN) {
            throw new DomainException("UNAUTHORIZED", 
                "Only the gym owner or admin can perform this operation");
        }
    }
}