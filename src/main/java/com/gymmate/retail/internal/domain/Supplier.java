package com.gymmate.retail.internal.domain;

import com.gymmate.shared.domain.TenantEntity;
import lombok.*;

/**
 * Supplier entity representing vendors/suppliers.
 * Extends TenantJpaEntity as suppliers are typically organisation-level.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class Supplier extends TenantEntity {

  // Note: organisationId is inherited from TenantJpaEntity

  private String name;

  private String code; // Internal supplier code

  private String description;

  // Contact information
  private String contactPerson;

  private String email;

  private String phone;

  private String mobilePhone;

  private String website;

  // Address
  private String address;

  private String city;

  private String state;

  private String country;

  private String postalCode;

  // Business details
  private String taxId;

  private String paymentTerms; // Net 30, Net 60, etc.

  @Builder.Default
  private String currency = "USD";

  private java.math.BigDecimal creditLimit;

  // Category
  private String supplierCategory; // equipment, supplements, apparel, etc.

  // Rating and notes
  @Builder.Default
  private Integer rating = 0; // 0-5 stars

  private String notes;

  @Builder.Default
  private boolean preferred = false;

  // Business methods
  public void updateContactInfo(String contactPerson, String email, String phone) {
    this.contactPerson = contactPerson;
    this.email = email;
    this.phone = phone;
  }

  public void updateAddress(String address, String city, String state, String country, String postalCode) {
    this.address = address;
    this.city = city;
    this.state = state;
    this.country = country;
    this.postalCode = postalCode;
  }

  public void setRating(int rating) {
    if (rating >= 0 && rating <= 5) {
      this.rating = rating;
    }
  }

  public void markAsPreferred() {
    this.preferred = true;
  }

  public void unmarkAsPreferred() {
    this.preferred = false;
  }
}
