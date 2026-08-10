/**
 * Breaks a direct gym&lt;-&gt;membership module cycle: {@code GymService} used to read
 * {@code membership.infrastructure.MemberInvoiceRepository} directly for revenue
 * analytics, while {@code membership.application.MemberPaymentService} also depends on
 * {@code gym} for Stripe Connect routing — a real 2-cycle
 * {@code ApplicationModules.verify()} correctly rejects. This port inverts the
 * analytics-read direction: {@code gym} depends on its own port, and
 * {@code membership.infrastructure} provides the adapter — same direction membership
 * already depends on gym in, consistent with the rest of the graph.
 */
@org.springframework.modulith.NamedInterface("application.port")
package com.gymmate.gym.application.port;
