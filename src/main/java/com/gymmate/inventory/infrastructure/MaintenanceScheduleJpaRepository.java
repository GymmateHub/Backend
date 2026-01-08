package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * JPA repository for MaintenanceSchedule entity.
 */
@Repository
public interface MaintenanceScheduleJpaRepository extends JpaRepository<MaintenanceSchedule, UUID> {

  List<MaintenanceSchedule> findByEquipmentId(UUID equipmentId);

  List<MaintenanceSchedule> findByGymId(UUID gymId);

  List<MaintenanceSchedule> findByOrganisationId(UUID organisationId);

  @Query("SELECT m FROM MaintenanceSchedule m WHERE m.gymId = :gymId AND m.completed = false")
  List<MaintenanceSchedule> findPendingByGymId(@Param("gymId") UUID gymId);

  @Query("SELECT m FROM MaintenanceSchedule m WHERE m.organisationId = :organisationId AND m.completed = false")
  List<MaintenanceSchedule> findPendingByOrganisationId(@Param("organisationId") UUID organisationId);

  @Query("SELECT m FROM MaintenanceSchedule m WHERE m.gymId = :gymId AND m.scheduledDate <= :date AND m.completed = false")
  List<MaintenanceSchedule> findDueByGymId(@Param("gymId") UUID gymId, @Param("date") LocalDate date);

  @Query("SELECT m FROM MaintenanceSchedule m WHERE m.organisationId = :organisationId AND m.scheduledDate <= :date AND m.completed = false")
  List<MaintenanceSchedule> findDueByOrganisationId(@Param("organisationId") UUID organisationId, @Param("date") LocalDate date);

  @Query("SELECT m FROM MaintenanceSchedule m WHERE m.gymId = :gymId AND m.scheduledDate BETWEEN :startDate AND :endDate")
  List<MaintenanceSchedule> findByGymIdAndDateRange(@Param("gymId") UUID gymId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

  long countByEquipmentId(UUID equipmentId);
}
