package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.Equipment;
import com.gymmate.inventory.domain.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for Equipment entity.
 */
@Repository
public interface EquipmentJpaRepository extends JpaRepository<Equipment, UUID> {

  List<Equipment> findByOrganisationId(UUID organisationId);

  List<Equipment> findByGymId(UUID gymId);

  List<Equipment> findByOrganisationIdAndGymId(UUID organisationId, UUID gymId);

  @Query("SELECT e FROM Equipment e WHERE e.organisationId = :organisationId AND e.active = true")
  List<Equipment> findActiveByOrganisationId(@Param("organisationId") UUID organisationId);

  @Query("SELECT e FROM Equipment e WHERE e.gymId = :gymId AND e.active = true")
  List<Equipment> findActiveByGymId(@Param("gymId") UUID gymId);

  List<Equipment> findByOrganisationIdAndStatus(UUID organisationId, EquipmentStatus status);

  List<Equipment> findByGymIdAndStatus(UUID gymId, EquipmentStatus status);

  @Query("SELECT e FROM Equipment e WHERE e.gymId = :gymId AND e.nextMaintenanceDate <= :date")
  List<Equipment> findMaintenanceDueByGymId(@Param("gymId") UUID gymId, @Param("date") LocalDate date);

  @Query("SELECT e FROM Equipment e WHERE e.organisationId = :organisationId AND e.nextMaintenanceDate <= :date")
  List<Equipment> findMaintenanceDueByOrganisationId(@Param("organisationId") UUID organisationId, @Param("date") LocalDate date);

  Optional<Equipment> findBySerialNumber(String serialNumber);

  long countByGymId(UUID gymId);

  long countByOrganisationId(UUID organisationId);

  boolean existsBySerialNumber(String serialNumber);
}
