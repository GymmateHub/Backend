package com.gymmate.gym.infrastructure;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.domain.GymStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<Gym> findByOwnerIdAndStatus(UUID ownerId, GymStatus status);

    @Query("SELECT COALESCE(SUM(g.maxMembers), 0) FROM Gym g WHERE g.ownerId = :ownerId")
    Integer sumMaxMembersByOwnerId(@Param("ownerId") UUID ownerId);

    @Query("SELECT COUNT(g) FROM Gym g WHERE g.ownerId = :ownerId AND g.status = :status")
    Long countByOwnerIdAndStatus(@Param("ownerId") UUID ownerId, @Param("status") GymStatus status);
}
