package com.gymmate.billing.internal.application.dto;

import java.time.LocalDateTime;

/**
 * Response containing Stripe Connect account status.
 */
public record ConnectAccountStatusResponse(
        String accountId,
        Boolean chargesEnabled,
        Boolean payoutsEnabled,
        Boolean detailsSubmitted,
        Boolean requiresAction,
        LocalDateTime currentDeadline,
        String dashboardUrl
) {
}
