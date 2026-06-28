package com.gymmate.access.application.port;

import com.gymmate.access.domain.AccessPoint;
import com.gymmate.access.domain.enums.AccessPointMode;

import java.util.Optional;

/**
 * Port abstracting the physical layer of an access point. The software core
 * depends only on this interface; concrete adapters (software, turnstile,
 * camera/CV) plug in without changing the decision logic.
 */
public interface AccessDevicePort {

  /** The mode this adapter handles; used to route a given access point. */
  AccessPointMode supportedMode();

  /** Open the point for exactly one passage (no-op for software-only). */
  void openOnce(AccessPoint accessPoint);

  /**
   * People detected passing for the current entry window, when the hardware can
   * report it (turnstile/CV). Empty when unsupported (software).
   */
  Optional<Integer> getPassCount(AccessPoint accessPoint);

  /** Whether the device backing this point is reachable. */
  boolean isOnline(AccessPoint accessPoint);
}
