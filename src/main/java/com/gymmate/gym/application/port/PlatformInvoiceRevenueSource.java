package com.gymmate.gym.application.port;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Total paid platform-subscription invoice revenue for an organisation over a
 * period — owned by {@code payment} (Stripe subscription invoices), consumed by
 * {@code GymService} for owner analytics. Same rationale as
 * {@link MembershipRevenueSource}: breaks a gym&lt;-&gt;payment module cycle.
 */
public interface PlatformInvoiceRevenueSource {
    BigDecimal sumPaidAmountByOrganisationIdAndPeriod(UUID organisationId, LocalDateTime periodStart, LocalDateTime periodEnd);
}
