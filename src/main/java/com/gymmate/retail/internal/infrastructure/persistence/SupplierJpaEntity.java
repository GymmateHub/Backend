package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.TenantJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import com.gymmate.retail.internal.domain.Supplier;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link Supplier} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "Supplier")
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(Supplier.class)
public class SupplierJpaEntity extends TenantJpaEntity {

    // Note: organisationId is inherited from TenantJpaEntity
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
    private String currency = "USD";

    @Column(name = "credit_limit", precision = 10, scale = 2)
    private java.math.BigDecimal creditLimit;

    // Category
    @Column(name = "supplier_category", length = 100)
    private String supplierCategory; // equipment, supplements, apparel, etc.

    // Rating and notes
    @Column(name = "rating")
    private Integer rating = 0; // 0-5 stars

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_preferred")
    private boolean preferred = false;
}
