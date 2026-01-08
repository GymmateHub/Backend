package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.Equipment;
import com.gymmate.inventory.domain.EquipmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter for Equipment repository.
 */
@Component
@RequiredArgsConstructor
public class EquipmentRepositoryAdapter implements EquipmentRepository {

  private final EquipmentJpaRepository jpaRepository;

  @Override
  public Equipment save(Equipment equipment) {
    return jpaRepository.save(equipment);
  }

  @Override
  public Optional<Equipment> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<Equipment> findByOrganisationId(UUID organisationId) {
    return jpaRepository.findByOrganisationId(organisationId);
  }

  @Override
  public List<Equipment> findByGymId(UUID gymId) {
    return jpaRepository.findByGymId(gymId);
  }

  @Override
  public List<Equipment> findByOrganisationIdAndGymId(UUID organisationId, UUID gymId) {
    return jpaRepository.findByOrganisationIdAndGymId(organisationId, gymId);
  }

  @Override
  public List<Equipment> findActiveByOrganisationId(UUID organisationId) {
    return jpaRepository.findActiveByOrganisationId(organisationId);
  }

  @Override
  public List<Equipment> findActiveByGymId(UUID gymId) {
    return jpaRepository.findActiveByGymId(gymId);
  }

  @Override
  public List<Equipment> findByOrganisationIdAndStatus(UUID organisationId, EquipmentStatus status) {
    return jpaRepository.findByOrganisationIdAndStatus(organisationId, status);
  }

  @Override
  public List<Equipment> findByGymIdAndStatus(UUID gymId, EquipmentStatus status) {
    return jpaRepository.findByGymIdAndStatus(gymId, status);
  }

  @Override
  public List<Equipment> findMaintenanceDueByGymId(UUID gymId, LocalDate date) {
    return jpaRepository.findMaintenanceDueByGymId(gymId, date);
  }

  @Override
  public List<Equipment> findMaintenanceDueByOrganisationId(UUID organisationId, LocalDate date) {
    return jpaRepository.findMaintenanceDueByOrganisationId(organisationId, date);
  }

  @Override
  public Optional<Equipment> findBySerialNumber(String serialNumber) {
    return jpaRepository.findBySerialNumber(serialNumber);
  }

  @Override
  public void delete(Equipment equipment) {
    jpaRepository.delete(equipment);
  }

  @Override
  public long countByGymId(UUID gymId) {
    return jpaRepository.countByGymId(gymId);
  }

  @Override
  public long countByOrganisationId(UUID organisationId) {
    return jpaRepository.countByOrganisationId(organisationId);
  }

  @Override
  public boolean existsBySerialNumber(String serialNumber) {
    return jpaRepository.existsBySerialNumber(serialNumber);
  }
}
