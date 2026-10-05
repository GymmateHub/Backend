package com.gymmate.billing.internal.domain;

import com.gymmate.shared.constants.PaymentMethodOwnerType;
import com.gymmate.shared.constants.PaymentMethodType;
import com.gymmate.shared.domain.BaseAuditEntity;
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
@EqualsAndHashCode(callSuper = true)
@Builder
public class PaymentMethod extends BaseAuditEntity {

    /**
     * Organisation ID - the billing entity that owns this payment method.
     * Primary filter for multi-tenant operations.
     */
    private UUID organisationId;

    /**
     * Type of owner: GYM, ORGANISATION, or MEMBER
     */
    private PaymentMethodOwnerType ownerType;

    /**
     * ID of the owner (organisation_id for ORGANISATION type, gym_id for GYM type,
     * member_id for MEMBER type)
     */
    private UUID ownerId;

    /**
     * Gym ID - optional, for gym-specific payment methods or member context
     */
    private UUID gymId;

    /**
     * Member ID - only populated for MEMBER type
     */
    private UUID memberId;

    // Payment provider details
    @Builder.Default
    private String provider = "stripe";

    private String providerPaymentMethodId;

    private String providerCustomerId;

    // Payment method type
    private PaymentMethodType methodType;

    // Card details (when method_type = CARD)
    private String cardBrand;

    private String cardLastFour;

    private Integer cardExpiresMonth;

    private Integer cardExpiresYear;

    // Bank details (when method_type = BANK_ACCOUNT)
    private String bankName;

    private String bankLastFour;

    // Wallet details (when method_type = DIGITAL_WALLET)
    private String walletType;

    private String walletEmail;

    // Status flags
    @Builder.Default
    private Boolean isDefault = false;

    // Note: isActive is inherited from BaseAuditJpaEntity (mapped to 'is_active'
    // column as 'active' field)

    @Builder.Default
    private Boolean isVerified = false;

    private LocalDateTime verifiedAt;

    // Metadata
    private String billingDetails;

    private String metadata;

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
     * BaseAuditJpaEntity
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
