package com.gymmate.retail.internal.application.port;

import com.gymmate.retail.internal.domain.MaintenanceRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
  
  List<MaintenanceRecord> saveAll(Iterable<MaintenanceRecord> entities);
  
  boolean existsById(UUID id);
  
  List<MaintenanceRecord> findAll();
  
  List<MaintenanceRecord> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void deleteAll(Iterable<MaintenanceRecord> entities);
  
  MaintenanceRecord saveAndFlush(MaintenanceRecord entity);
  
  void flush();
  
  Page<MaintenanceRecord> findAll(Pageable pageable);
}
