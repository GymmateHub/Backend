package com.gymmate.gym.infrastructure;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.domain.GymStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for Gym entity.
 */
@Repository
public interface GymJpaRepository extends JpaRepository<Gym, UUID> {

    List<Gym> findByOwnerId(UUID ownerId);

    List<Gym> findByStatus(GymStatus status);

    List<Gym> findByCity(String city);
}
