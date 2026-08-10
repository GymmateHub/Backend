/**
 * Tier-1 named interface: {@code NotificationService}/{@code EmailService} etc are
 * already called as facades from most other modules to raise in-app/SSE notifications
 * and send email — the architecturally-correct pattern, so exposing it endorses
 * existing practice. See docs/adr/0001-modulith-phase0.md.
 */
@org.springframework.modulith.NamedInterface("application")
package com.gymmate.notification.application;
