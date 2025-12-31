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

  @Query("SELECT CASE WHEN COUNT(gc) > 0 THEN true ELSE false END FROM GymClass gc JOIN ClassCategory cc ON gc.categoryId = cc.id WHERE cc.gymId = :gymId AND gc.name = :name")
  boolean existsByGymIdAndName(@Param("gymId") UUID gymId, @Param("name") String name);

  @Query("SELECT COUNT(gc) FROM GymClass gc JOIN ClassCategory cc ON gc.categoryId = cc.id WHERE cc.gymId = :gymId")
  long countByGymId(@Param("gymId") UUID gymId);

  @Query("SELECT gc FROM GymClass gc JOIN ClassCategory cc ON gc.categoryId = cc.id WHERE cc.gymId = :gymId AND gc.name = :name")
  Optional<GymClass> findByGymIdAndName(@Param("gymId") UUID gymId, @Param("name") String name);

  @Query("SELECT gc FROM GymClass gc JOIN ClassCategory cc ON gc.categoryId = cc.id WHERE cc.gymId = :gymId AND gc.active = true")
  List<GymClass> findActiveByGymId(@Param("gymId") UUID gymId);

  @Query("SELECT gc FROM GymClass gc JOIN ClassCategory cc ON gc.categoryId = cc.id WHERE cc.gymId = :gymId")
  List<GymClass> findByGymId(@Param("gymId") UUID gymId);

  List<GymClass> findByCategoryId(UUID categoryId);
}




