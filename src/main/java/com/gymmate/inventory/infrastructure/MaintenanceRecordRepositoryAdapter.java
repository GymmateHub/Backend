package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.MaintenanceRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter for MaintenanceRecord repository.
 */
@Component
@RequiredArgsConstructor
public class MaintenanceRecordRepositoryAdapter implements MaintenanceRecordRepository {

  private final MaintenanceRecordJpaRepository jpaRepository;

  @Override
  public MaintenanceRecord save(MaintenanceRecord maintenanceRecord) {
    return jpaRepository.save(maintenanceRecord);
  }

  @Override
  public Optional<MaintenanceRecord> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<MaintenanceRecord> findByEquipmentId(UUID equipmentId) {
    return jpaRepository.findByEquipmentId(equipmentId);
  }

  @Override
  public List<MaintenanceRecord> findByGymId(UUID gymId) {
    return jpaRepository.findByGymId(gymId);
  }

  @Override
  public List<MaintenanceRecord> findByOrganisationId(UUID organisationId) {
    return jpaRepository.findByOrganisationId(organisationId);
  }

  @Override
  public List<MaintenanceRecord> findByEquipmentIdOrderByMaintenanceDateDesc(UUID equipmentId) {
    return jpaRepository.findByEquipmentIdOrderByMaintenanceDateDesc(equipmentId);
  }

  @Override
  public List<MaintenanceRecord> findByGymIdAndDateRange(UUID gymId, LocalDate startDate, LocalDate endDate) {
    return jpaRepository.findByGymIdAndDateRange(gymId, startDate, endDate);
  }

  @Override
  public List<MaintenanceRecord> findByOrganisationIdAndDateRange(UUID organisationId, LocalDate startDate, LocalDate endDate) {
    return jpaRepository.findByOrganisationIdAndDateRange(organisationId, startDate, endDate);
  }

  @Override
  public List<MaintenanceRecord> findIncompleteByGymId(UUID gymId) {
    return jpaRepository.findIncompleteByGymId(gymId);
  }

  @Override
  public void delete(MaintenanceRecord maintenanceRecord) {
    jpaRepository.delete(maintenanceRecord);
  }

  @Override
  public long countByEquipmentId(UUID equipmentId) {
    return jpaRepository.countByEquipmentId(equipmentId);
  }
}
