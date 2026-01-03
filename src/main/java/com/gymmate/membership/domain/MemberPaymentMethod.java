package com.gymmate.membership.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entity representing a payment method for a member.
 * These payment methods are stored on the gym's Stripe Connect account.
 * Extends GymScopedEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "member_payment_methods")
public class MemberPaymentMethod extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedEntity)
    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "stripe_payment_method_id", nullable = false)
    private String stripePaymentMethodId;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "card_brand", length = 50)
    private String cardBrand;

    @Column(name = "last_four", length = 4)
    private String lastFour;

    @Column(name = "expiry_month")
    private Integer expiryMonth;

    @Column(name = "expiry_year")
    private Integer expiryYear;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void removeDefault() {
        this.isDefault = false;
    }
}

