package com.gymmate.inventory.application;

import com.gymmate.inventory.domain.Equipment;
import com.gymmate.inventory.domain.EquipmentStatus;
import com.gymmate.inventory.infrastructure.EquipmentRepository;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Application service for equipment management use cases.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EquipmentService {

  private final EquipmentRepository equipmentRepository;

  /**
   * Create new equipment.
   */
  @Transactional
  public Equipment createEquipment(Equipment equipment) {
    // Validate serial number uniqueness if provided
    if (equipment.getSerialNumber() != null && 
        equipmentRepository.existsBySerialNumber(equipment.getSerialNumber())) {
      throw new DomainException("DUPLICATE_SERIAL_NUMBER", 
        "Equipment with this serial number already exists");
    }

    log.info("Creating equipment: {} for organisation: {}, gym: {}", 
      equipment.getName(), equipment.getOrganisationId(), equipment.getGymId());
    return equipmentRepository.save(equipment);
  }

  /**
   * Update equipment details.
   */
  @Transactional
  public Equipment updateEquipment(UUID id, Equipment updatedEquipment) {
    Equipment existing = getEquipmentById(id);
    
    // Validate serial number uniqueness if changed
    if (updatedEquipment.getSerialNumber() != null && 
        !updatedEquipment.getSerialNumber().equals(existing.getSerialNumber()) &&
        equipmentRepository.existsBySerialNumber(updatedEquipment.getSerialNumber())) {
      throw new DomainException("DUPLICATE_SERIAL_NUMBER", 
        "Equipment with this serial number already exists");
    }

    // Update fields
    existing.setName(updatedEquipment.getName());
    existing.setCategory(updatedEquipment.getCategory());
    existing.setDescription(updatedEquipment.getDescription());
    existing.setManufacturer(updatedEquipment.getManufacturer());
    existing.setModel(updatedEquipment.getModel());
    existing.setSerialNumber(updatedEquipment.getSerialNumber());
    existing.setLocationNotes(updatedEquipment.getLocationNotes());
    existing.setAreaId(updatedEquipment.getAreaId());
    existing.setNotes(updatedEquipment.getNotes());
    existing.setImageUrl(updatedEquipment.getImageUrl());

    log.info("Updated equipment: {}", id);
    return equipmentRepository.save(existing);
  }

  /**
   * Update equipment status.
   */
  @Transactional
  public Equipment updateEquipmentStatus(UUID id, EquipmentStatus status) {
    Equipment equipment = getEquipmentById(id);
    equipment.updateStatus(status);
    log.info("Updated equipment {} status to: {}", id, status);
    return equipmentRepository.save(equipment);
  }

  /**
   * Record maintenance on equipment.
   */
  @Transactional
  public Equipment recordMaintenance(UUID id, LocalDate maintenanceDate, BigDecimal cost) {
    Equipment equipment = getEquipmentById(id);
    equipment.recordMaintenance(maintenanceDate, cost);
    log.info("Recorded maintenance for equipment: {}", id);
    return equipmentRepository.save(equipment);
  }

  /**
   * Update usage hours for equipment.
   */
  @Transactional
  public Equipment updateUsageHours(UUID id, int hours) {
    Equipment equipment = getEquipmentById(id);
    equipment.updateUsageHours(hours);
    return equipmentRepository.save(equipment);
  }

  /**
   * Retire equipment.
   */
  @Transactional
  public Equipment retireEquipment(UUID id) {
    Equipment equipment = getEquipmentById(id);
    equipment.retire();
    log.info("Retired equipment: {}", id);
    return equipmentRepository.save(equipment);
  }

  /**
   * Mark equipment as available.
   */
  @Transactional
  public Equipment markAsAvailable(UUID id) {
    Equipment equipment = getEquipmentById(id);
    equipment.markAsAvailable();
    log.info("Marked equipment {} as available", id);
    return equipmentRepository.save(equipment);
  }

  /**
   * Mark equipment in use.
   */
  @Transactional
  public Equipment markInUse(UUID id) {
    Equipment equipment = getEquipmentById(id);
    equipment.markInUse();
    return equipmentRepository.save(equipment);
  }

  /**
   * Mark equipment for maintenance.
   */
  @Transactional
  public Equipment markForMaintenance(UUID id) {
    Equipment equipment = getEquipmentById(id);
    equipment.markForMaintenance();
    log.info("Marked equipment {} for maintenance", id);
    return equipmentRepository.save(equipment);
  }

  /**
   * Delete equipment.
   */
  @Transactional
  public void deleteEquipment(UUID id) {
    Equipment equipment = getEquipmentById(id);
    equipmentRepository.delete(equipment);
    log.info("Deleted equipment: {}", id);
  }

  /**
   * Get equipment by ID.
   */
  public Equipment getEquipmentById(UUID id) {
    return equipmentRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Equipment", id.toString()));
  }

  /**
   * Get all equipment for organisation.
   */
  public List<Equipment> getEquipmentByOrganisation(UUID organisationId) {
    return equipmentRepository.findByOrganisationId(organisationId);
  }

  /**
   * Get all equipment for gym.
   */
  public List<Equipment> getEquipmentByGym(UUID gymId) {
    return equipmentRepository.findByGymId(gymId);
  }

  /**
   * Get active equipment for organisation.
   */
  public List<Equipment> getActiveEquipmentByOrganisation(UUID organisationId) {
    return equipmentRepository.findActiveByOrganisationId(organisationId);
  }

  /**
   * Get active equipment for gym.
   */
  public List<Equipment> getActiveEquipmentByGym(UUID gymId) {
    return equipmentRepository.findActiveByGymId(gymId);
  }

  /**
   * Get equipment by status for organisation.
   */
  public List<Equipment> getEquipmentByOrganisationAndStatus(UUID organisationId, EquipmentStatus status) {
    return equipmentRepository.findByOrganisationIdAndStatus(organisationId, status);
  }

  /**
   * Get equipment by status for gym.
   */
  public List<Equipment> getEquipmentByGymAndStatus(UUID gymId, EquipmentStatus status) {
    return equipmentRepository.findByGymIdAndStatus(gymId, status);
  }

  /**
   * Get equipment due for maintenance by gym.
   */
  public List<Equipment> getMaintenanceDueByGym(UUID gymId) {
    return equipmentRepository.findMaintenanceDueByGymId(gymId, LocalDate.now());
  }

  /**
   * Get equipment due for maintenance by organisation.
   */
  public List<Equipment> getMaintenanceDueByOrganisation(UUID organisationId) {
    return equipmentRepository.findMaintenanceDueByOrganisationId(organisationId, LocalDate.now());
  }

  /**
   * Get equipment count by gym.
   */
  public long countByGym(UUID gymId) {
    return equipmentRepository.countByGymId(gymId);
  }

  /**
   * Get equipment count by organisation.
   */
  public long countByOrganisation(UUID organisationId) {
    return equipmentRepository.countByOrganisationId(organisationId);
  }
}
