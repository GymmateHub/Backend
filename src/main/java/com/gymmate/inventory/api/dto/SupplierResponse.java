package com.gymmate.inventory.api.dto;

import com.gymmate.inventory.domain.Supplier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for supplier response.
 */
public record SupplierResponse(
  UUID id,
  String name,
  String code,
  String description,
  String contactPerson,
  String email,
  String phone,
  String mobilePhone,
  String website,
  String address,
  String city,
  String state,
  String country,
  String postalCode,
  String taxId,
  String paymentTerms,
  String currency,
  BigDecimal creditLimit,
  String supplierCategory,
  Integer rating,
  String notes,
  boolean preferred,
  UUID organisationId,
  LocalDateTime createdAt,
  LocalDateTime updatedAt,
  boolean active
) {
  public static SupplierResponse fromEntity(Supplier supplier) {
    return new SupplierResponse(
      supplier.getId(),
      supplier.getName(),
      supplier.getCode(),
      supplier.getDescription(),
      supplier.getContactPerson(),
      supplier.getEmail(),
      supplier.getPhone(),
      supplier.getMobilePhone(),
      supplier.getWebsite(),
      supplier.getAddress(),
      supplier.getCity(),
      supplier.getState(),
      supplier.getCountry(),
      supplier.getPostalCode(),
      supplier.getTaxId(),
      supplier.getPaymentTerms(),
      supplier.getCurrency(),
      supplier.getCreditLimit(),
      supplier.getSupplierCategory(),
      supplier.getRating(),
      supplier.getNotes(),
      supplier.isPreferred(),
      supplier.getOrganisationId(),
      supplier.getCreatedAt(),
      supplier.getUpdatedAt(),
      supplier.isActive()
    );
  }
}
