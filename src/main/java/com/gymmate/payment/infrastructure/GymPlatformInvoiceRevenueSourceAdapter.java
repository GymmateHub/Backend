package com.gymmate.payment.infrastructure;

import com.gymmate.gym.application.port.PlatformInvoiceRevenueSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implements gym's {@link PlatformInvoiceRevenueSource} port using this module's own
 * {@code GymInvoiceRepository} — see the port Javadoc for why this moved out of
 * {@code GymService} calling the repository directly.
 */
@Component
@RequiredArgsConstructor
public class GymPlatformInvoiceRevenueSourceAdapter implements PlatformInvoiceRevenueSource {

    private final GymInvoiceRepository gymInvoiceRepository;

    @Override
    public BigDecimal sumPaidAmountByOrganisationIdAndPeriod(UUID organisationId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        return gymInvoiceRepository.sumPaidAmountByOrganisationIdAndPeriod(organisationId, periodStart, periodEnd);
    }
}
