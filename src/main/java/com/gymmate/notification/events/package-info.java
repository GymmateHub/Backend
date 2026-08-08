/**
 * Domain event contracts other modules publish against (e.g.
 * {@code eventPublisher.publishEvent(PaymentFailedEvent.builder()...)} from the
 * {@code payment} module). This is the intended, designed-for-crossing-boundaries
 * surface of the notification module — not Tier-1 debt like the other grants in this
 * pass.
 */
@org.springframework.modulith.NamedInterface("events")
package com.gymmate.notification.events;
