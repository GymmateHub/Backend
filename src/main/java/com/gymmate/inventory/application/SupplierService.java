package com.gymmate.inventory.application;

import com.gymmate.inventory.domain.Supplier;
import com.gymmate.inventory.infrastructure.SupplierRepository;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Application service for supplier management use cases.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SupplierService {

  private final SupplierRepository supplierRepository;

  /**
   * Create new supplier.
   */
  @Transactional
  public Supplier createSupplier(Supplier supplier) {
    // Validate code uniqueness if provided
    if (supplier.getCode() != null && supplierRepository.existsByCode(supplier.getCode())) {
      throw new DomainException("DUPLICATE_SUPPLIER_CODE", 
        "Supplier with this code already exists");
    }

    // Validate name uniqueness within organisation
    if (supplier.getOrganisationId() != null) {
      supplierRepository.findByOrganisationIdAndName(supplier.getOrganisationId(), supplier.getName())
        .ifPresent(existing -> {
          throw new DomainException("DUPLICATE_SUPPLIER_NAME", 
            "Supplier with this name already exists in your organisation");
        });
    }

    log.info("Creating supplier: {} for organisation: {}", 
      supplier.getName(), supplier.getOrganisationId());
    return supplierRepository.save(supplier);
  }

  /**
   * Update supplier.
   */
  @Transactional
  public Supplier updateSupplier(UUID id, Supplier updatedSupplier) {
    Supplier existing = getSupplierById(id);

    // Validate code uniqueness if changed
    if (updatedSupplier.getCode() != null && 
        !updatedSupplier.getCode().equals(existing.getCode()) &&
        supplierRepository.existsByCode(updatedSupplier.getCode())) {
      throw new DomainException("DUPLICATE_SUPPLIER_CODE", 
        "Supplier with this code already exists");
    }

    // Validate name uniqueness if changed
    if (!updatedSupplier.getName().equals(existing.getName())) {
      supplierRepository.findByOrganisationIdAndName(existing.getOrganisationId(), updatedSupplier.getName())
        .ifPresent(duplicate -> {
          throw new DomainException("DUPLICATE_SUPPLIER_NAME", 
            "Supplier with this name already exists in your organisation");
        });
    }

    // Update fields
    existing.setName(updatedSupplier.getName());
    existing.setCode(updatedSupplier.getCode());
    existing.setDescription(updatedSupplier.getDescription());
    existing.setContactPerson(updatedSupplier.getContactPerson());
    existing.setEmail(updatedSupplier.getEmail());
    existing.setPhone(updatedSupplier.getPhone());
    existing.setMobilePhone(updatedSupplier.getMobilePhone());
    existing.setWebsite(updatedSupplier.getWebsite());
    existing.setTaxId(updatedSupplier.getTaxId());
    existing.setPaymentTerms(updatedSupplier.getPaymentTerms());
    existing.setCurrency(updatedSupplier.getCurrency());
    existing.setCreditLimit(updatedSupplier.getCreditLimit());
    existing.setSupplierCategory(updatedSupplier.getSupplierCategory());
    existing.setNotes(updatedSupplier.getNotes());

    log.info("Updated supplier: {}", id);
    return supplierRepository.save(existing);
  }

  /**
   * Update supplier contact information.
   */
  @Transactional
  public Supplier updateSupplierContact(UUID id, String contactPerson, String email, String phone) {
    Supplier supplier = getSupplierById(id);
    supplier.updateContactInfo(contactPerson, email, phone);
    log.info("Updated contact info for supplier: {}", id);
    return supplierRepository.save(supplier);
  }

  /**
   * Update supplier address.
   */
  @Transactional
  public Supplier updateSupplierAddress(UUID id, String address, String city, 
                                         String state, String country, String postalCode) {
    Supplier supplier = getSupplierById(id);
    supplier.updateAddress(address, city, state, country, postalCode);
    log.info("Updated address for supplier: {}", id);
    return supplierRepository.save(supplier);
  }

  /**
   * Set supplier rating.
   */
  @Transactional
  public Supplier setSupplierRating(UUID id, int rating) {
    Supplier supplier = getSupplierById(id);
    supplier.setRating(rating);
    log.info("Set rating {} for supplier: {}", rating, id);
    return supplierRepository.save(supplier);
  }

  /**
   * Mark supplier as preferred.
   */
  @Transactional
  public Supplier markAsPreferred(UUID id) {
    Supplier supplier = getSupplierById(id);
    supplier.markAsPreferred();
    log.info("Marked supplier {} as preferred", id);
    return supplierRepository.save(supplier);
  }

  /**
   * Unmark supplier as preferred.
   */
  @Transactional
  public Supplier unmarkAsPreferred(UUID id) {
    Supplier supplier = getSupplierById(id);
    supplier.unmarkAsPreferred();
    log.info("Unmarked supplier {} as preferred", id);
    return supplierRepository.save(supplier);
  }

  /**
   * Deactivate supplier.
   */
  @Transactional
  public Supplier deactivateSupplier(UUID id) {
    Supplier supplier = getSupplierById(id);
    supplier.deactivate();
    log.info("Deactivated supplier: {}", id);
    return supplierRepository.save(supplier);
  }

  /**
   * Activate supplier.
   */
  @Transactional
  public Supplier activateSupplier(UUID id) {
    Supplier supplier = getSupplierById(id);
    supplier.activate();
    log.info("Activated supplier: {}", id);
    return supplierRepository.save(supplier);
  }

  /**
   * Delete supplier.
   */
  @Transactional
  public void deleteSupplier(UUID id) {
    Supplier supplier = getSupplierById(id);
    supplierRepository.delete(supplier);
    log.info("Deleted supplier: {}", id);
  }

  /**
   * Get supplier by ID.
   */
  public Supplier getSupplierById(UUID id) {
    return supplierRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Supplier", id.toString()));
  }

  /**
   * Get supplier by code.
   */
  public Supplier getSupplierByCode(String code) {
    return supplierRepository.findByCode(code)
      .orElseThrow(() -> new ResourceNotFoundException("Supplier with code", code));
  }

  /**
   * Get all suppliers for organisation.
   */
  public List<Supplier> getSuppliersByOrganisation(UUID organisationId) {
    return supplierRepository.findByOrganisationId(organisationId);
  }

  /**
   * Get active suppliers for organisation.
   */
  public List<Supplier> getActiveSuppliersByOrganisation(UUID organisationId) {
    return supplierRepository.findActiveByOrganisationId(organisationId);
  }

  /**
   * Get preferred suppliers for organisation.
   */
  public List<Supplier> getPreferredSuppliersByOrganisation(UUID organisationId) {
    return supplierRepository.findPreferredByOrganisationId(organisationId);
  }

  /**
   * Get supplier count for organisation.
   */
  public long countByOrganisation(UUID organisationId) {
    return supplierRepository.countByOrganisationId(organisationId);
  }
}
