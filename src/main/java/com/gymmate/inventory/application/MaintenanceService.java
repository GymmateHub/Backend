package com.gymmate.inventory.application;

import com.gymmate.inventory.domain.Equipment;
import com.gymmate.inventory.domain.MaintenanceRecord;
import com.gymmate.inventory.domain.MaintenanceSchedule;
import com.gymmate.inventory.infrastructure.EquipmentRepository;
import com.gymmate.inventory.infrastructure.MaintenanceRecordRepository;
import com.gymmate.inventory.infrastructure.MaintenanceScheduleRepository;
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
 * Application service for maintenance management use cases.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MaintenanceService {

  private final MaintenanceRecordRepository maintenanceRecordRepository;
  private final MaintenanceScheduleRepository maintenanceScheduleRepository;
  private final EquipmentRepository equipmentRepository;

  // ===== Maintenance Record Operations =====

  /**
   * Create maintenance record.
   */
  @Transactional
  public MaintenanceRecord createMaintenanceRecord(MaintenanceRecord record) {
    // Validate equipment exists
    Equipment equipment = equipmentRepository.findById(record.getEquipmentId())
      .orElseThrow(() -> new ResourceNotFoundException("Equipment", record.getEquipmentId().toString()));

    log.info("Creating maintenance record for equipment: {}", record.getEquipmentId());
    MaintenanceRecord saved = maintenanceRecordRepository.save(record);

    // Update equipment maintenance info if completed
    if (record.isCompleted() && record.getCost() != null) {
      equipment.recordMaintenance(record.getMaintenanceDate(), record.getCost());
      equipmentRepository.save(equipment);
    }

    return saved;
  }

  /**
   * Update maintenance record.
   */
  @Transactional
  public MaintenanceRecord updateMaintenanceRecord(UUID id, MaintenanceRecord updatedRecord) {
    MaintenanceRecord existing = getMaintenanceRecordById(id);

    existing.setMaintenanceDate(updatedRecord.getMaintenanceDate());
    existing.setMaintenanceType(updatedRecord.getMaintenanceType());
    existing.setDescription(updatedRecord.getDescription());
    existing.setPerformedBy(updatedRecord.getPerformedBy());
    existing.setTechnicianCompany(updatedRecord.getTechnicianCompany());
    existing.setCost(updatedRecord.getCost());
    existing.setPartsReplaced(updatedRecord.getPartsReplaced());
    existing.setNextMaintenanceDue(updatedRecord.getNextMaintenanceDue());
    existing.setNotes(updatedRecord.getNotes());
    existing.setInvoiceNumber(updatedRecord.getInvoiceNumber());
    existing.setInvoiceUrl(updatedRecord.getInvoiceUrl());

    log.info("Updated maintenance record: {}", id);
    return maintenanceRecordRepository.save(existing);
  }

  /**
   * Complete maintenance record.
   */
  @Transactional
  public MaintenanceRecord completeMaintenanceRecord(UUID id, String completionNotes) {
    MaintenanceRecord record = getMaintenanceRecordById(id);
    record.complete(completionNotes);
    
    log.info("Completed maintenance record: {}", id);
    MaintenanceRecord saved = maintenanceRecordRepository.save(record);

    // Update equipment
    Equipment equipment = equipmentRepository.findById(record.getEquipmentId())
      .orElseThrow(() -> new ResourceNotFoundException("Equipment", record.getEquipmentId().toString()));
    equipment.recordMaintenance(record.getMaintenanceDate(), record.getCost());
    equipmentRepository.save(equipment);

    return saved;
  }

  /**
   * Delete maintenance record.
   */
  @Transactional
  public void deleteMaintenanceRecord(UUID id) {
    MaintenanceRecord record = getMaintenanceRecordById(id);
    maintenanceRecordRepository.delete(record);
    log.info("Deleted maintenance record: {}", id);
  }

  /**
   * Get maintenance record by ID.
   */
  public MaintenanceRecord getMaintenanceRecordById(UUID id) {
    return maintenanceRecordRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRecord", id.toString()));
  }

  /**
   * Get maintenance records for equipment.
   */
  public List<MaintenanceRecord> getMaintenanceRecordsByEquipment(UUID equipmentId) {
    return maintenanceRecordRepository.findByEquipmentIdOrderByMaintenanceDateDesc(equipmentId);
  }

  /**
   * Get maintenance records for gym.
   */
  public List<MaintenanceRecord> getMaintenanceRecordsByGym(UUID gymId) {
    return maintenanceRecordRepository.findByGymId(gymId);
  }

  /**
   * Get maintenance records for organisation.
   */
  public List<MaintenanceRecord> getMaintenanceRecordsByOrganisation(UUID organisationId) {
    return maintenanceRecordRepository.findByOrganisationId(organisationId);
  }

  /**
   * Get maintenance records for gym within date range.
   */
  public List<MaintenanceRecord> getMaintenanceRecordsByGymAndDateRange(
      UUID gymId, LocalDate startDate, LocalDate endDate) {
    return maintenanceRecordRepository.findByGymIdAndDateRange(gymId, startDate, endDate);
  }

  /**
   * Get incomplete maintenance records for gym.
   */
  public List<MaintenanceRecord> getIncompleteMaintenanceRecordsByGym(UUID gymId) {
    return maintenanceRecordRepository.findIncompleteByGymId(gymId);
  }

  // ===== Maintenance Schedule Operations =====

  /**
   * Create maintenance schedule.
   */
  @Transactional
  public MaintenanceSchedule createMaintenanceSchedule(MaintenanceSchedule schedule) {
    // Validate equipment exists
    equipmentRepository.findById(schedule.getEquipmentId())
      .orElseThrow(() -> new ResourceNotFoundException("Equipment", schedule.getEquipmentId().toString()));

    log.info("Creating maintenance schedule for equipment: {}", schedule.getEquipmentId());
    return maintenanceScheduleRepository.save(schedule);
  }

  /**
   * Update maintenance schedule.
   */
  @Transactional
  public MaintenanceSchedule updateMaintenanceSchedule(UUID id, MaintenanceSchedule updatedSchedule) {
    MaintenanceSchedule existing = getMaintenanceScheduleById(id);

    existing.setScheduleName(updatedSchedule.getScheduleName());
    existing.setDescription(updatedSchedule.getDescription());
    existing.setScheduledDate(updatedSchedule.getScheduledDate());
    existing.setMaintenanceType(updatedSchedule.getMaintenanceType());
    existing.setAssignedTo(updatedSchedule.getAssignedTo());
    existing.setEstimatedDurationHours(updatedSchedule.getEstimatedDurationHours());
    existing.setRecurring(updatedSchedule.isRecurring());
    existing.setRecurrenceIntervalDays(updatedSchedule.getRecurrenceIntervalDays());
    existing.setNotes(updatedSchedule.getNotes());

    log.info("Updated maintenance schedule: {}", id);
    return maintenanceScheduleRepository.save(existing);
  }

  /**
   * Complete maintenance schedule.
   */
  @Transactional
  public MaintenanceSchedule completeMaintenanceSchedule(UUID id, UUID maintenanceRecordId) {
    MaintenanceSchedule schedule = getMaintenanceScheduleById(id);
    
    // Validate maintenance record exists
    maintenanceRecordRepository.findById(maintenanceRecordId)
      .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRecord", maintenanceRecordId.toString()));

    schedule.complete(maintenanceRecordId);
    log.info("Completed maintenance schedule: {}", id);
    
    MaintenanceSchedule saved = maintenanceScheduleRepository.save(schedule);

    // Create next occurrence if recurring
    if (schedule.isRecurring() && schedule.getRecurrenceIntervalDays() != null) {
      MaintenanceSchedule nextSchedule = MaintenanceSchedule.builder()
        .equipmentId(schedule.getEquipmentId())
        .scheduleName(schedule.getScheduleName())
        .description(schedule.getDescription())
        .scheduledDate(schedule.getScheduledDate().plusDays(schedule.getRecurrenceIntervalDays()))
        .maintenanceType(schedule.getMaintenanceType())
        .assignedTo(schedule.getAssignedTo())
        .estimatedDurationHours(schedule.getEstimatedDurationHours())
        .recurring(true)
        .recurrenceIntervalDays(schedule.getRecurrenceIntervalDays())
        .notes(schedule.getNotes())
        .build();
      
      nextSchedule.setOrganisationId(schedule.getOrganisationId());
      nextSchedule.setGymId(schedule.getGymId());
      
      maintenanceScheduleRepository.save(nextSchedule);
      log.info("Created next recurring maintenance schedule for equipment: {}", schedule.getEquipmentId());
    }

    return saved;
  }

  /**
   * Reschedule maintenance.
   */
  @Transactional
  public MaintenanceSchedule rescheduleMaintenance(UUID id, LocalDate newDate) {
    MaintenanceSchedule schedule = getMaintenanceScheduleById(id);
    schedule.reschedule(newDate);
    log.info("Rescheduled maintenance: {} to {}", id, newDate);
    return maintenanceScheduleRepository.save(schedule);
  }

  /**
   * Send reminder for maintenance schedule.
   */
  @Transactional
  public MaintenanceSchedule sendReminder(UUID id) {
    MaintenanceSchedule schedule = getMaintenanceScheduleById(id);
    schedule.sendReminder();
    log.info("Sent reminder for maintenance schedule: {}", id);
    return maintenanceScheduleRepository.save(schedule);
  }

  /**
   * Delete maintenance schedule.
   */
  @Transactional
  public void deleteMaintenanceSchedule(UUID id) {
    MaintenanceSchedule schedule = getMaintenanceScheduleById(id);
    maintenanceScheduleRepository.delete(schedule);
    log.info("Deleted maintenance schedule: {}", id);
  }

  /**
   * Get maintenance schedule by ID.
   */
  public MaintenanceSchedule getMaintenanceScheduleById(UUID id) {
    return maintenanceScheduleRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("MaintenanceSchedule", id.toString()));
  }

  /**
   * Get maintenance schedules for equipment.
   */
  public List<MaintenanceSchedule> getMaintenanceSchedulesByEquipment(UUID equipmentId) {
    return maintenanceScheduleRepository.findByEquipmentId(equipmentId);
  }

  /**
   * Get pending maintenance schedules for gym.
   */
  public List<MaintenanceSchedule> getPendingMaintenanceSchedulesByGym(UUID gymId) {
    return maintenanceScheduleRepository.findPendingByGymId(gymId);
  }

  /**
   * Get due maintenance schedules for gym.
   */
  public List<MaintenanceSchedule> getDueMaintenanceSchedulesByGym(UUID gymId) {
    return maintenanceScheduleRepository.findDueByGymId(gymId, LocalDate.now());
  }

  /**
   * Get maintenance schedules for gym within date range.
   */
  public List<MaintenanceSchedule> getMaintenanceSchedulesByGymAndDateRange(
      UUID gymId, LocalDate startDate, LocalDate endDate) {
    return maintenanceScheduleRepository.findByGymIdAndDateRange(gymId, startDate, endDate);
  }
}
