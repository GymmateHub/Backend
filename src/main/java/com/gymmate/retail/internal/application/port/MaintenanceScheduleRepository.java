package com.gymmate.retail.internal.application.port;

import com.gymmate.retail.internal.domain.MaintenanceSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
  
  List<MaintenanceSchedule> saveAll(Iterable<MaintenanceSchedule> entities);
  
  boolean existsById(UUID id);
  
  List<MaintenanceSchedule> findAll();
  
  List<MaintenanceSchedule> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void deleteAll(Iterable<MaintenanceSchedule> entities);
  
  MaintenanceSchedule saveAndFlush(MaintenanceSchedule entity);
  
  void flush();
  
  Page<MaintenanceSchedule> findAll(Pageable pageable);
}
