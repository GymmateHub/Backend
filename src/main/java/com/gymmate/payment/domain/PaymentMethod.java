package com.gymmate.payment.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Unified entity representing a payment method for both gyms and members.
 *
 * Payment Flow Types:
 * - GYM_PLATFORM: Gym pays GymMate platform (subscription billing)
 * - MEMBER_PAYMENT: Member pays Gym (membership fees, class bookings, etc.)
 *
 * This single source of truth enables:
 * - Unified reporting and analytics
 * - Consistent payment method management
 * - Easier auditing and compliance
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "payment_methods", indexes = {
        @Index(name = "idx_pm_owner", columnList = "owner_type, owner_id"),
        @Index(name = "idx_pm_organisation", columnList = "organisation_id"),
        @Index(name = "idx_pm_gym", columnList = "gym_id"),
        @Index(name = "idx_pm_provider_id", columnList = "provider_payment_method_id"),
        @Index(name = "idx_pm_default", columnList = "owner_type, owner_id, is_default")
})
public class PaymentMethod extends BaseAuditEntity {

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
    @Builder.Default
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
    @Builder.Default
    private Boolean isDefault = false;

    // Note: isActive is inherited from BaseAuditEntity (mapped to 'is_active'
    // column as 'active' field)

    @Column(name = "is_verified")
    @Builder.Default
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

    // ============================================
    // Convenience factory methods
    // ============================================

    /**
     * Create a payment method for an organisation (platform payments)
     */
    public static PaymentMethod forOrganisation(UUID organisationId, UUID gymId, String providerPaymentMethodId,
            PaymentMethodType methodType) {
        return PaymentMethod.builder()
                .ownerType(PaymentMethodOwnerType.ORGANISATION)
                .ownerId(organisationId)
                .organisationId(organisationId)
                .gymId(gymId)
                .providerPaymentMethodId(providerPaymentMethodId)
                .methodType(methodType)
                .build();
    }

    /**
     * Create a payment method for a member (gym payments)
     */
    public static PaymentMethod forMember(UUID memberId, UUID gymId, String providerPaymentMethodId,
            PaymentMethodType methodType) {
        return PaymentMethod.builder()
                .ownerType(PaymentMethodOwnerType.MEMBER)
                .ownerId(memberId)
                .gymId(gymId)
                .memberId(memberId)
                .providerPaymentMethodId(providerPaymentMethodId)
                .methodType(methodType)
                .build();
    }

    // ============================================
    // Convenience methods
    // ============================================

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void removeDefault() {
        this.isDefault = false;
    }

    public void activate() {
        this.setActive(true);
    }

    public void deactivate() {
        this.setActive(false);
    }

    /**
     * Accessor for isActive - delegates to inherited 'active' field from
     * BaseAuditEntity
     */
    public Boolean getIsActive() {
        return this.isActive();
    }

    public void setIsActive(Boolean isActive) {
        this.setActive(isActive != null && isActive);
    }

    public void markVerified() {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    public boolean isOrganisationPaymentMethod() {
        return PaymentMethodOwnerType.ORGANISATION.equals(this.ownerType);
    }

    public boolean isCard() {
        return PaymentMethodType.CARD.equals(this.methodType);
    }

    public boolean isBankAccount() {
        return PaymentMethodType.BANK_ACCOUNT.equals(this.methodType);
    }

    // Aliases for backward compatibility
    public String getStripePaymentMethodId() {
        return this.providerPaymentMethodId;
    }

    public void setStripePaymentMethodId(String stripePaymentMethodId) {
        this.providerPaymentMethodId = stripePaymentMethodId;
    }

    public String getLastFour() {
        if (isCard()) {
            return this.cardLastFour;
        } else if (isBankAccount()) {
            return this.bankLastFour;
        }
        return null;
    }

    public Integer getExpiryMonth() {
        return this.cardExpiresMonth;
    }

    public Integer getExpiryYear() {
        return this.cardExpiresYear;
    }
}
