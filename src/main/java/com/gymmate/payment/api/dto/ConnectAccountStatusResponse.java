package com.gymmate.payment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response containing Stripe Connect account status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectAccountStatusResponse {

    private String accountId;
    private Boolean chargesEnabled;
    private Boolean payoutsEnabled;
    private Boolean detailsSubmitted;
    private Boolean requiresAction;
    private LocalDateTime currentDeadline;
    private String dashboardUrl;
}

