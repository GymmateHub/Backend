package com.gymmate.billing.internal.application.port;

import com.gymmate.billing.internal.domain.GymPaymentAccount;

import java.util.Optional;
import java.util.UUID;

/** Outbound port: gyms as Stripe Connect payees. */
public interface GymPaymentAccountPort {

    Optional<GymPaymentAccount> findById(UUID gymId);

    /** Persists the Stripe Connect state of the account; contact data is not writable. */
    GymPaymentAccount save(GymPaymentAccount account);
}
