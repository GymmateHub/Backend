/**
 * Tier-1 named interface: {@code PaymentNotificationService}/{@code StripePaymentService}/
 * {@code StripeConnectService} are already used as facades from notification,
 * subscription, and membership — the architecturally-correct pattern, so exposing it
 * endorses existing practice. See docs/adr/0001-modulith-phase0.md.
 */
@org.springframework.modulith.NamedInterface("application")
package com.gymmate.payment.application;
