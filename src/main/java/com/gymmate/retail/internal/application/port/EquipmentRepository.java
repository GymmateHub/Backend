package com.gymmate.retail.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.retail.internal.domain.Equipment;
import com.gymmate.retail.internal.domain.EquipmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Equipment domain entity.
 */
public interface EquipmentRepository extends DomainRepository<Equipment, UUID> {

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

  long countByGymId(UUID gymId);

  long countByOrganisationId(UUID organisationId);

  boolean existsBySerialNumber(String serialNumber);
}
