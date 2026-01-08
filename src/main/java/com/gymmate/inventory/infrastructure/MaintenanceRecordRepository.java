package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.MaintenanceRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for MaintenanceRecord domain entity.
 */
public interface MaintenanceRecordRepository {

  MaintenanceRecord save(MaintenanceRecord maintenanceRecord);

  Optional<MaintenanceRecord> findById(UUID id);

  List<MaintenanceRecord> findByEquipmentId(UUID equipmentId);

  List<MaintenanceRecord> findByGymId(UUID gymId);

  List<MaintenanceRecord> findByOrganisationId(UUID organisationId);

  List<MaintenanceRecord> findByEquipmentIdOrderByMaintenanceDateDesc(UUID equipmentId);

  List<MaintenanceRecord> findByGymIdAndDateRange(UUID gymId, LocalDate startDate, LocalDate endDate);

  List<MaintenanceRecord> findByOrganisationIdAndDateRange(UUID organisationId, LocalDate startDate, LocalDate endDate);

  List<MaintenanceRecord> findIncompleteByGymId(UUID gymId);

  void delete(MaintenanceRecord maintenanceRecord);

  long countByEquipmentId(UUID equipmentId);
}
