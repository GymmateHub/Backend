package com.gymmate.membership.domain;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Gym aggregate.
 */
public interface GymRepository {
    
    /**
     * Save a gym to the repository.
     */
    Gym save(Gym gym);
    
    /**
     * Find a gym by its unique identifier.
     */
    Optional<Gym> findById(Long id);
    
    /**
     * Find all gyms owned by a specific user.
     */
    List<Gym> findByOwnerId(Long ownerId);
    
    /**
     * Find all gyms with a specific status.
     */
    List<Gym> findByStatus(GymStatus status);
    
    /**
     * Find gyms by city.
     */
    List<Gym> findByAddressCity(String city);
    
    /**
     * Find all gyms.
     */
    List<Gym> findAll();
    
    /**
     * Delete a gym by its identifier.
     */
    void deleteById(Long id);
    
    /**
     * Check if a gym exists with the given ID.
     */
    boolean existsById(Long id);
}