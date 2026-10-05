package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.constants.PaymentMethodOwnerType;
import com.gymmate.shared.constants.PaymentMethodType;
import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.billing.internal.domain.PaymentMethod;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link PaymentMethod} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "PaymentMethod")
@Table(name = "payment_methods", indexes = { @Index(name = "idx_pm_owner", columnList = "owner_type, owner_id"), @Index(name = "idx_pm_organisation", columnList = "organisation_id"), @Index(name = "idx_pm_gym", columnList = "gym_id"), @Index(name = "idx_pm_provider_id", columnList = "provider_payment_method_id"), @Index(name = "idx_pm_default", columnList = "owner_type, owner_id, is_default") })
@Getter
@Setter
@NoArgsConstructor
@DomainModel(PaymentMethod.class)
public class // ============================================
// Convenience factory methods
// ============================================
// ============================================
// Convenience methods
// ============================================
PaymentMethodJpaEntity extends BaseAuditJpaEntity {

    /**
     * Organisation ID - the billing entity that owns this payment method.
     * Primary filter for multi-tenant operations.
     */
    @Column(name = "organisation_id")
    private UUID organisationId;

    /**
     * Type of owner: GYM, ORGANISATION, or MEMBER
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 20)
    private PaymentMethodOwnerType ownerType;

    /**
     * ID of the owner (organisation_id for ORGANISATION type, gym_id for GYM type,
     * member_id for MEMBER type)
     */
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    /**
     * Gym ID - optional, for gym-specific payment methods or member context
     */
    @Column(name = "gym_id")
    private UUID gymId;

    /**
     * Member ID - only populated for MEMBER type
     */
    @Column(name = "member_id")
    private UUID memberId;

    // Payment provider details
    @Column(length = 50)
    private String provider = "stripe";

    @Column(name = "provider_payment_method_id", nullable = false)
    private String providerPaymentMethodId;

    @Column(name = "provider_customer_id")
    private String providerCustomerId;

    // Payment method type
    @Enumerated(EnumType.STRING)
    @Column(name = "method_type", nullable = false, length = 20)
    private PaymentMethodType methodType;

    // Card details (when method_type = CARD)
    @Column(name = "card_brand", length = 50)
    private String cardBrand;

    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;

    @Column(name = "card_expires_month")
    private Integer cardExpiresMonth;

    @Column(name = "card_expires_year")
    private Integer cardExpiresYear;

    // Bank details (when method_type = BANK_ACCOUNT)
    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_last_four", length = 4)
    private String bankLastFour;

    // Wallet details (when method_type = DIGITAL_WALLET)
    @Column(name = "wallet_type", length = 50)
    private String walletType;

    @Column(name = "wallet_email")
    private String walletEmail;

    // Status flags
    @Column(name = "is_default")
    private Boolean isDefault = false;

    // Note: isActive is inherited from BaseAuditJpaEntity (mapped to 'is_active'
    // column as 'active' field)
    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    // Metadata
    @Column(name = "billing_details", columnDefinition = "TEXT")
    private String billingDetails;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(length = 500)
    private String description;
}
