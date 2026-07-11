package com.gymmate.access.domain.enums;

/**
 * Enforcement mode for an access point. Selects which {@code AccessDevicePort}
 * adapter handles the physical open + pass-count reconciliation.
 */
public enum AccessPointMode {
  /** Software only — no hardware. QR/passcode scan via phone or kiosk. */
  SOFTWARE,
  /** Physical turnstile / maglock controller. */
  TURNSTILE,
  /** Camera / computer-vision people-counting + image capture. */
  CV
}
