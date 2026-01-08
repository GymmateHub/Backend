package com.gymmate.inventory.domain;

import com.gymmate.shared.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Supplier entity representing vendors/suppliers.
 * Extends TenantEntity as suppliers are typically organisation-level.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "suppliers")
public class Supplier extends TenantEntity {

  // Note: organisationId is inherited from TenantEntity

  @Column(nullable = false, length = 200)
  private String name;

  @Column(length = 100)
  private String code; // Internal supplier code

  @Column(columnDefinition = "TEXT")
  private String description;

  // Contact information
  @Column(name = "contact_person", length = 200)
  private String contactPerson;

  @Column(length = 100)
  private String email;

  @Column(length = 20)
  private String phone;

  @Column(name = "mobile_phone", length = 20)
  private String mobilePhone;

  @Column(length = 500)
  private String website;

  // Address
  @Column(columnDefinition = "TEXT")
  private String address;

  @Column(length = 100)
  private String city;

  @Column(length = 50)
  private String state;

  @Column(length = 50)
  private String country;

  @Column(name = "postal_code", length = 20)
  private String postalCode;

  // Business details
  @Column(name = "tax_id", length = 100)
  private String taxId;

  @Column(name = "payment_terms", length = 100)
  private String paymentTerms; // Net 30, Net 60, etc.

  @Column(name = "currency", length = 3)
  @Builder.Default
  private String currency = "USD";

  @Column(name = "credit_limit", precision = 10, scale = 2)
  private java.math.BigDecimal creditLimit;

  // Category
  @Column(name = "supplier_category", length = 100)
  private String supplierCategory; // equipment, supplements, apparel, etc.

  // Rating and notes
  @Column(name = "rating")
  @Builder.Default
  private Integer rating = 0; // 0-5 stars

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "is_preferred")
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
