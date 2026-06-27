package com.gymmate.access.infrastructure;

import com.gymmate.access.application.port.AccessDevicePort;
import com.gymmate.access.domain.AccessPoint;
import com.gymmate.access.domain.enums.AccessPointMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Default zero-hardware adapter. Entry is enforced entirely in software via
 * QR/passcode scans from a phone or kiosk — no turnstile required. Hardware
 * adapters (turnstile, CV) implement {@link AccessDevicePort} for other modes.
 */
@Slf4j
@Component
public class SoftwareAccessAdapter implements AccessDevicePort {

  @Override
  public AccessPointMode supportedMode() {
    return AccessPointMode.SOFTWARE;
  }

  @Override
  public void openOnce(AccessPoint accessPoint) {
    // No physical lock to drive; the grant itself is the "open".
    log.debug("Software access point '{}' opened (one passage)", accessPoint.getName());
  }

  @Override
  public Optional<Integer> getPassCount(AccessPoint accessPoint) {
    // Software has no people-counter; reconciliation is unavailable.
    return Optional.empty();
  }

  @Override
  public boolean isOnline(AccessPoint accessPoint) {
    return true;
  }
}
