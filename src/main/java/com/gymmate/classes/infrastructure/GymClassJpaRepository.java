package com.gymmate.classes.infrastructure;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import com.gymmate.classes.domain.GymClass;
@Repository
public interface GymClassJpaRepository extends JpaRepository<GymClass, UUID> {
  boolean existsByGymIdAndName(UUID gymId, String name);
  long countByGymId(UUID gymId);
  Optional<GymClass> findByGymIdAndName(UUID gymId, String name);

    @Query("SELECT gc FROM GymClass gc WHERE gc.gymId = :gymId AND gc.active = true")
  List<GymClass> findActiveByGymId(@Param("gymId") UUID gymId);

  List<GymClass> findByCategoryId(UUID categoryId);
  List<GymClass> findByGymId(UUID gymId);
}




