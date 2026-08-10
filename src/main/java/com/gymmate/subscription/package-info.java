/**
 * Platform SaaS billing: organisation subscription tiers, usage tracking, rate
 * limits. Declared {@code OPEN} (exempt from Modulith's cycle detection, same
 * mechanism as {@code shared}): {@code payment}'s Stripe webhook handling exists
 * specifically to keep {@code Subscription} state in sync with Stripe, and
 * {@code StripePaymentService} reads {@code Subscription} to manage Stripe
 * customer/subscription IDs — subscription state genuinely IS payment's core write
 * target here, not a misplaced dependency to invert. Modulith has no primitive for
 * "these two top-level packages are one logical module" short of physically
 * colocating them (a real refactor, out of Phase 0 scope) or a custom module
 * detection strategy; {@code Type.OPEN} achieves the same practical effect —
 * {@code payment}<->{@code subscription} stop failing {@code verify()} — without
 * moving files. Revisit if/when payment's webhook handling is decoupled from direct
 * Subscription writes (see membership.application.MembershipPaymentEventListener for
 * the pattern that would apply).
 */
@org.springframework.modulith.ApplicationModule(
    type = org.springframework.modulith.ApplicationModule.Type.OPEN,
    displayName = "Platform Subscription"
)
package com.gymmate.subscription;
