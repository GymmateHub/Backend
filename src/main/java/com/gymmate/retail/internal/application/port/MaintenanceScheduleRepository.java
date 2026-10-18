package com.gymmate.retail.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.retail.internal.domain.MaintenanceSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for MaintenanceSchedule domain entity.
 */
public interface MaintenanceScheduleRepository extends DomainRepository<MaintenanceSchedule, UUID> {

  List<MaintenanceSchedule> findByEquipmentId(UUID equipmentId);

  List<MaintenanceSchedule> findByGymId(UUID gymId);

  List<MaintenanceSchedule> findByOrganisationId(UUID organisationId);

  List<MaintenanceSchedule> findPendingByGymId(UUID gymId);

  List<MaintenanceSchedule> findPendingByOrganisationId(UUID organisationId);

  List<MaintenanceSchedule> findDueByGymId(UUID gymId, LocalDate date);

  List<MaintenanceSchedule> findDueByOrganisationId(UUID organisationId, LocalDate date);

  List<MaintenanceSchedule> findByGymIdAndDateRange(UUID gymId, LocalDate startDate, LocalDate endDate);

  long countByEquipmentId(UUID equipmentId);
}
