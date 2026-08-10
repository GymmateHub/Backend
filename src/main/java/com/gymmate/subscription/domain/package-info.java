/**
 * Tier-1 named interface (Phase 0 documented debt — see docs/adr/0001-modulith-phase0.md):
 * {@code payment} mutates {@code Subscription} state directly from Stripe webhook
 * handling (markPastDue/activate/suspend) — the single largest coupling cluster found
 * in the Phase 0 baseline scan (186 call sites), reflecting that payment IS what
 * drives subscription state transitions, not a layering mistake to paper over here.
 */
@org.springframework.modulith.NamedInterface("domain")
package com.gymmate.subscription.domain;
