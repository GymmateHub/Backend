package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.retail.internal.domain.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for Equipment entity.
 */
@Repository
public interface EquipmentJpaRepository extends JpaRepository<EquipmentJpaEntity, UUID> {

    List<EquipmentJpaEntity> findByOrganisationId(UUID organisationId);

    List<EquipmentJpaEntity> findByGymId(UUID gymId);

    List<EquipmentJpaEntity> findByOrganisationIdAndGymId(UUID organisationId, UUID gymId);

    @Query("SELECT e FROM Equipment e WHERE e.organisationId = :organisationId AND e.active = true")
    List<EquipmentJpaEntity> findActiveByOrganisationId(@Param("organisationId") UUID organisationId);

    @Query("SELECT e FROM Equipment e WHERE e.gymId = :gymId AND e.active = true")
    List<EquipmentJpaEntity> findActiveByGymId(@Param("gymId") UUID gymId);

    List<EquipmentJpaEntity> findByOrganisationIdAndStatus(UUID organisationId, EquipmentStatus status);

    List<EquipmentJpaEntity> findByGymIdAndStatus(UUID gymId, EquipmentStatus status);

    @Query("SELECT e FROM Equipment e WHERE e.gymId = :gymId AND e.nextMaintenanceDate <= :date")
    List<EquipmentJpaEntity> findMaintenanceDueByGymId(@Param("gymId") UUID gymId, @Param("date") LocalDate date);

    @Query("SELECT e FROM Equipment e WHERE e.organisationId = :organisationId AND e.nextMaintenanceDate <= :date")
    List<EquipmentJpaEntity> findMaintenanceDueByOrganisationId(@Param("organisationId") UUID organisationId, @Param("date") LocalDate date);

    Optional<EquipmentJpaEntity> findBySerialNumber(String serialNumber);

    long countByGymId(UUID gymId);

    long countByOrganisationId(UUID organisationId);

    boolean existsBySerialNumber(String serialNumber);
}
