/**
 * Tier-1 named interface (Phase 0 documented debt — see docs/adr/0001-modulith-phase0.md):
 * {@code User}/{@code Member}/{@code Staff}/{@code Trainer} are referenced as JPA
 * relationship targets and read models from most other modules.
 */
@org.springframework.modulith.NamedInterface("domain")
package com.gymmate.user.domain;
