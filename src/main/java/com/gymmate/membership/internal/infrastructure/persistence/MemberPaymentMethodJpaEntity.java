package com.gymmate.membership.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import com.gymmate.membership.internal.domain.MemberPaymentMethod;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link MemberPaymentMethod} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "MemberPaymentMethod")
@Table(name = "member_payment_methods")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(MemberPaymentMethod.class)
public class MemberPaymentMethodJpaEntity extends GymScopedJpaEntity {

    // Note: gymId is inherited from GymScopedJpaEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
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
    private Boolean isDefault = false;
}
