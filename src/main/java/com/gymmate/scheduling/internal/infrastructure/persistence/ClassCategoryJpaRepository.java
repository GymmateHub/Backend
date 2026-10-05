package com.gymmate.scheduling.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassCategoryJpaRepository extends JpaRepository<ClassCategoryJpaEntity, UUID> {

    List<ClassCategoryJpaEntity> findByGymId(UUID gymId);

    @Query("SELECT cc FROM ClassCategory cc WHERE cc.gymId = :gymId AND cc.active = true")
    List<ClassCategoryJpaEntity> findActiveByGymId(@Param("gymId") UUID gymId);

    Optional<ClassCategoryJpaEntity> findByGymIdAndName(UUID gymId, String name);

    boolean existsByGymIdAndName(UUID gymId, String name);
}
