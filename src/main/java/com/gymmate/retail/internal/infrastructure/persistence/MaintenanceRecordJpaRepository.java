package com.gymmate.retail.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * JPA repository for MaintenanceRecord entity.
 */
@Repository
public interface MaintenanceRecordJpaRepository extends JpaRepository<MaintenanceRecordJpaEntity, UUID> {

    List<MaintenanceRecordJpaEntity> findByEquipmentId(UUID equipmentId);

    List<MaintenanceRecordJpaEntity> findByGymId(UUID gymId);

    List<MaintenanceRecordJpaEntity> findByOrganisationId(UUID organisationId);

    @Query("SELECT m FROM MaintenanceRecord m WHERE m.equipmentId = :equipmentId ORDER BY m.maintenanceDate DESC")
    List<MaintenanceRecordJpaEntity> findByEquipmentIdOrderByMaintenanceDateDesc(@Param("equipmentId") UUID equipmentId);

    @Query("SELECT m FROM MaintenanceRecord m WHERE m.gymId = :gymId AND m.maintenanceDate BETWEEN :startDate AND :endDate")
    List<MaintenanceRecordJpaEntity> findByGymIdAndDateRange(@Param("gymId") UUID gymId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT m FROM MaintenanceRecord m WHERE m.organisationId = :organisationId AND m.maintenanceDate BETWEEN :startDate AND :endDate")
    List<MaintenanceRecordJpaEntity> findByOrganisationIdAndDateRange(@Param("organisationId") UUID organisationId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT m FROM MaintenanceRecord m WHERE m.gymId = :gymId AND m.completed = false")
    List<MaintenanceRecordJpaEntity> findIncompleteByGymId(@Param("gymId") UUID gymId);

    long countByEquipmentId(UUID equipmentId);
}
