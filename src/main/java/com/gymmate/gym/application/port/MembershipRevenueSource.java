package com.gymmate.gym.application.port;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Total paid member-invoice revenue for a gym over a period — owned by
 * {@code membership} (paid invoices are membership data), consumed by
 * {@code GymService} for owner/gym analytics. See the port package Javadoc for why
 * this exists as a port rather than a direct repository read.
 */
public interface MembershipRevenueSource {
    BigDecimal sumPaidAmountByGymIdAndPeriod(UUID gymId, LocalDateTime periodStart, LocalDateTime periodEnd);
}
