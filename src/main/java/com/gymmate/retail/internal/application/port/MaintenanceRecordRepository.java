package com.gymmate.retail.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.retail.internal.domain.MaintenanceRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for MaintenanceRecord domain entity.
 */
public interface MaintenanceRecordRepository extends DomainRepository<MaintenanceRecord, UUID> {

  List<MaintenanceRecord> findByEquipmentId(UUID equipmentId);

  List<MaintenanceRecord> findByGymId(UUID gymId);

  List<MaintenanceRecord> findByOrganisationId(UUID organisationId);

  List<MaintenanceRecord> findByEquipmentIdOrderByMaintenanceDateDesc(UUID equipmentId);

  List<MaintenanceRecord> findByGymIdAndDateRange(UUID gymId, LocalDate startDate, LocalDate endDate);

  List<MaintenanceRecord> findByOrganisationIdAndDateRange(UUID organisationId, LocalDate startDate, LocalDate endDate);

  List<MaintenanceRecord> findIncompleteByGymId(UUID gymId);

  long countByEquipmentId(UUID equipmentId);
}
