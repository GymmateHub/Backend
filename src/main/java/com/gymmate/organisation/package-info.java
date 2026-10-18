/**
 * Tenant root: organisations and the gyms (locations) they own, plan limits and
 * gym context. Merged from the former {@code organisation} and {@code gym} modules (ADR 0002).
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Organisation & Gyms"
)
package com.gymmate.organisation;
