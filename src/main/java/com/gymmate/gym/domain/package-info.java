/**
 * Tier-1 named interface (Phase 0 documented debt — see docs/adr/0001-modulith-phase0.md):
 * {@code Gym} is referenced as a JPA relationship target and read model from most
 * other modules.
 */
@org.springframework.modulith.NamedInterface("domain")
package com.gymmate.gym.domain;
