package com.gymmate.inventory.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * DTO for creating supplier.
 */
public record SupplierCreateRequest(
  @NotBlank(message = "Supplier name is required")
  String name,

  String code,
  String description,
  String contactPerson,

  @Email(message = "Invalid email format")
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
  String notes
) {
}
