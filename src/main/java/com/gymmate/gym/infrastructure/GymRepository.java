package com.gymmate.gym.infrastructure;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.domain.GymStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Gym aggregate (moved to infrastructure).
 */
public interface GymRepository {

    Gym save(Gym gym);

    Optional<Gym> findById(UUID id);

    List<Gym> findByOwnerId(UUID ownerId);

    List<Gym> findByStatus(GymStatus status);

    List<Gym> findByAddressCity(String city);

    List<Gym> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);
}

