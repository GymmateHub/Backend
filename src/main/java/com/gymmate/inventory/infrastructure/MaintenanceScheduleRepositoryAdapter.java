package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.MaintenanceSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter for MaintenanceSchedule repository.
 */
@Component
@RequiredArgsConstructor
public class MaintenanceScheduleRepositoryAdapter implements MaintenanceScheduleRepository {

  private final MaintenanceScheduleJpaRepository jpaRepository;

  @Override
  public MaintenanceSchedule save(MaintenanceSchedule maintenanceSchedule) {
    return jpaRepository.save(maintenanceSchedule);
  }

  @Override
  public Optional<MaintenanceSchedule> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<MaintenanceSchedule> findByEquipmentId(UUID equipmentId) {
    return jpaRepository.findByEquipmentId(equipmentId);
  }

  @Override
  public List<MaintenanceSchedule> findByGymId(UUID gymId) {
    return jpaRepository.findByGymId(gymId);
  }

  @Override
  public List<MaintenanceSchedule> findByOrganisationId(UUID organisationId) {
    return jpaRepository.findByOrganisationId(organisationId);
  }

  @Override
  public List<MaintenanceSchedule> findPendingByGymId(UUID gymId) {
    return jpaRepository.findPendingByGymId(gymId);
  }

  @Override
  public List<MaintenanceSchedule> findPendingByOrganisationId(UUID organisationId) {
    return jpaRepository.findPendingByOrganisationId(organisationId);
  }

  @Override
  public List<MaintenanceSchedule> findDueByGymId(UUID gymId, LocalDate date) {
    return jpaRepository.findDueByGymId(gymId, date);
  }

  @Override
  public List<MaintenanceSchedule> findDueByOrganisationId(UUID organisationId, LocalDate date) {
    return jpaRepository.findDueByOrganisationId(organisationId, date);
  }

  @Override
  public List<MaintenanceSchedule> findByGymIdAndDateRange(UUID gymId, LocalDate startDate, LocalDate endDate) {
    return jpaRepository.findByGymIdAndDateRange(gymId, startDate, endDate);
  }

  @Override
  public void delete(MaintenanceSchedule maintenanceSchedule) {
    jpaRepository.delete(maintenanceSchedule);
  }

  @Override
  public long countByEquipmentId(UUID equipmentId) {
    return jpaRepository.countByEquipmentId(equipmentId);
  }
}
