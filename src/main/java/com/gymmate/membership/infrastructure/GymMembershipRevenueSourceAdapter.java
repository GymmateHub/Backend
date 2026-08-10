package com.gymmate.membership.infrastructure;

import com.gymmate.gym.application.port.MembershipRevenueSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implements gym's {@link MembershipRevenueSource} port using this module's own
 * {@code MemberInvoiceRepository} — see the port package Javadoc for why this moved
 * out of {@code GymService} calling the repository directly.
 */
@Component
@RequiredArgsConstructor
public class GymMembershipRevenueSourceAdapter implements MembershipRevenueSource {

    private final MemberInvoiceRepository memberInvoiceRepository;

    @Override
    public BigDecimal sumPaidAmountByGymIdAndPeriod(UUID gymId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        return memberInvoiceRepository.sumPaidAmountByGymIdAndPeriod(gymId, periodStart, periodEnd);
    }
}
