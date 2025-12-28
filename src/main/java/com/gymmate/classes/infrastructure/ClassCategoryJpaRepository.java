package com.gymmate.classes.infrastructure;

import com.gymmate.classes.domain.ClassCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassCategoryJpaRepository extends JpaRepository<ClassCategory, UUID> {
  List<ClassCategory> findByGymId(UUID gymId);

  @Query("SELECT cc FROM ClassCategory cc WHERE cc.gymId = :gymId AND cc.active = true")
  List<ClassCategory> findActiveByGymId(@Param("gymId") UUID gymId);

  Optional<ClassCategory> findByGymIdAndName(UUID gymId, String name);
  boolean existsByGymIdAndName(UUID gymId, String name);
}

