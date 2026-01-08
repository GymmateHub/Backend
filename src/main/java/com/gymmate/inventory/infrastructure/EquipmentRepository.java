package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.Equipment;
import com.gymmate.inventory.domain.EquipmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Equipment domain entity.
 */
public interface EquipmentRepository {

  Equipment save(Equipment equipment);

  Optional<Equipment> findById(UUID id);

  List<Equipment> findByOrganisationId(UUID organisationId);

  List<Equipment> findByGymId(UUID gymId);

  List<Equipment> findByOrganisationIdAndGymId(UUID organisationId, UUID gymId);

  List<Equipment> findActiveByOrganisationId(UUID organisationId);

  List<Equipment> findActiveByGymId(UUID gymId);

  List<Equipment> findByOrganisationIdAndStatus(UUID organisationId, EquipmentStatus status);

  List<Equipment> findByGymIdAndStatus(UUID gymId, EquipmentStatus status);

  List<Equipment> findMaintenanceDueByGymId(UUID gymId, LocalDate date);

  List<Equipment> findMaintenanceDueByOrganisationId(UUID organisationId, LocalDate date);

  Optional<Equipment> findBySerialNumber(String serialNumber);

  void delete(Equipment equipment);

  long countByGymId(UUID gymId);

  long countByOrganisationId(UUID organisationId);

  boolean existsBySerialNumber(String serialNumber);
}
