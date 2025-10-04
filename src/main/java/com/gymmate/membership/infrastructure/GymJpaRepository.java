package com.gymmate.membership.infrastructure;

import com.gymmate.membership.domain.Gym;
import com.gymmate.membership.domain.GymStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Gym entity.
 */
@Repository
public interface GymJpaRepository extends JpaRepository<Gym, Long> {
    
    List<Gym> findByOwnerId(Long ownerId);
    
    List<Gym> findByStatus(GymStatus status);
    
    List<Gym> findByAddress_City(String city);
}