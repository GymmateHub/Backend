/**
 * Tier-1 named interface: {@code MemberService} etc are already used as facades by
 * several modules — this is the architecturally-correct pattern (calling a service,
 * not a repository), so exposing it endorses existing practice rather than
 * compromising. See docs/adr/0001-modulith-phase0.md.
 */
@org.springframework.modulith.NamedInterface("application")
package com.gymmate.user.application;
