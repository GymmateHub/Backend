/**
 * Identity: users, members, staff, trainers, invites, authentication (login, JWT, OTP,
 * password reset/policy, token rotation) and the HTTP security filter chain.
 * Merged from the former {@code user} module and {@code shared.security} (ADR 0002).
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Identity & Access Management"
)
package com.gymmate.identity;
