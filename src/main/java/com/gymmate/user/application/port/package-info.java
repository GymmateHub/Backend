/**
 * Breaks a direct gym&lt;-&gt;user module cycle: {@code InviteService} used to call
 * {@code gym.application.GymService} directly (just to get a gym's name/organisation
 * for invite emails), while {@code gym.application.GymService} also depends on
 * {@code user} (owner validation, analytics) — a real 2-cycle
 * {@code ApplicationModules.verify()} correctly rejects. This port inverts the smaller
 * of the two edges: {@code user} depends on its own port, {@code gym.infrastructure}
 * provides the adapter.
 */
@org.springframework.modulith.NamedInterface("application.port")
package com.gymmate.user.application.port;
