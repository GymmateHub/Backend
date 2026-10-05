package com.gymmate.membership.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.util.UUID;

/**
 * Entity representing a payment method for a member.
 * These payment methods are stored on the gym's Stripe Connect account.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class MemberPaymentMethod extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedJpaEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
    private UUID memberId;

    private String stripePaymentMethodId;

    private String type;

    private String cardBrand;

    private String lastFour;

    private Integer expiryMonth;

    private Integer expiryYear;

    @Builder.Default
    private 
    Boolean isDefault = false;

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void removeDefault() {
        this.isDefault = false;
    }
}
