package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.MaintenanceRecord;
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
public interface MaintenanceRecordJpaRepository extends JpaRepository<MaintenanceRecord, UUID> {

  List<MaintenanceRecord> findByEquipmentId(UUID equipmentId);

  List<MaintenanceRecord> findByGymId(UUID gymId);

  List<MaintenanceRecord> findByOrganisationId(UUID organisationId);

  @Query("SELECT m FROM MaintenanceRecord m WHERE m.equipmentId = :equipmentId ORDER BY m.maintenanceDate DESC")
  List<MaintenanceRecord> findByEquipmentIdOrderByMaintenanceDateDesc(@Param("equipmentId") UUID equipmentId);

  @Query("SELECT m FROM MaintenanceRecord m WHERE m.gymId = :gymId AND m.maintenanceDate BETWEEN :startDate AND :endDate")
  List<MaintenanceRecord> findByGymIdAndDateRange(@Param("gymId") UUID gymId, 
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

  @Query("SELECT m FROM MaintenanceRecord m WHERE m.organisationId = :organisationId AND m.maintenanceDate BETWEEN :startDate AND :endDate")
  List<MaintenanceRecord> findByOrganisationIdAndDateRange(@Param("organisationId") UUID organisationId,
                                                            @Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate);

  @Query("SELECT m FROM MaintenanceRecord m WHERE m.gymId = :gymId AND m.completed = false")
  List<MaintenanceRecord> findIncompleteByGymId(@Param("gymId") UUID gymId);

  long countByEquipmentId(UUID equipmentId);
}
