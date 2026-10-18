/**
 * Read-only reporting: gym dashboards/KPIs (membership, classes, POS, inventory) and
 * platform-admin views across organisations. Consumes other modules only through
 * their public facades. Merged from the former {@code analytics} and {@code admin}
 * modules (ADR 0002).
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Reporting"
)
package com.gymmate.reporting;
