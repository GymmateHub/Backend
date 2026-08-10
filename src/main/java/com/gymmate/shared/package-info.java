/**
 * Cross-cutting infrastructure: multitenancy ({@link com.gymmate.shared.multitenancy}),
 * security, base entity types, shared constants/DTOs/exceptions, config. Declared
 * {@code OPEN}: every other module may depend on it freely; it is a shared kernel,
 * not boundary-verified against other modules the way feature modules are.
 *
 * <p>{@code Type.OPEN} does not enforce it, but as a matter of code-review discipline,
 * {@code shared.security.service.*} / {@code shared.security.repository.*} should still
 * only be touched via their intended entry points (the auth API, the exposed
 * {@code PasswordEncoder}/{@code AuthenticationManager} beans) rather than instantiated
 * directly from other modules.
 */
@org.springframework.modulith.ApplicationModule(
    type = org.springframework.modulith.ApplicationModule.Type.OPEN,
    displayName = "Shared Kernel"
)
package com.gymmate.shared;
