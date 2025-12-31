package com.gymmate.classes.infrastructure;

import com.gymmate.classes.domain.GymArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GymAreaJpaRepository extends JpaRepository<GymArea, UUID> {
  List<GymArea> findByGymId(UUID gymId);
  boolean existsByGymIdAndName(UUID gymId, String name);
  Optional<GymArea> findByGymIdAndName(UUID gymId, String name);
}
