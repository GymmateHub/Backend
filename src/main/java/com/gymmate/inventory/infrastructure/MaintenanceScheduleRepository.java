package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.MaintenanceSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for MaintenanceSchedule domain entity.
 */
public interface MaintenanceScheduleRepository {

  MaintenanceSchedule save(MaintenanceSchedule maintenanceSchedule);

  Optional<MaintenanceSchedule> findById(UUID id);

  List<MaintenanceSchedule> findByEquipmentId(UUID equipmentId);

  List<MaintenanceSchedule> findByGymId(UUID gymId);

  List<MaintenanceSchedule> findByOrganisationId(UUID organisationId);

  List<MaintenanceSchedule> findPendingByGymId(UUID gymId);

  List<MaintenanceSchedule> findPendingByOrganisationId(UUID organisationId);

  List<MaintenanceSchedule> findDueByGymId(UUID gymId, LocalDate date);

  List<MaintenanceSchedule> findDueByOrganisationId(UUID organisationId, LocalDate date);

  List<MaintenanceSchedule> findByGymIdAndDateRange(UUID gymId, LocalDate startDate, LocalDate endDate);

  void delete(MaintenanceSchedule maintenanceSchedule);

  long countByEquipmentId(UUID equipmentId);
}
