package com.gymmate.membership.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled tasks for membership management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipScheduledTasks {

  private final MembershipService membershipService;

  /**
   * Auto-unfreeze memberships that have passed their freeze end date.
   * Runs daily at 1 AM.
   */
  @Scheduled(cron = "0 0 1 * * *") // Daily at 1 AM
  @Transactional
  public void processExpiredFreezes() {
    log.info("Starting scheduled task: Process expired membership freezes");
    try {
      int unfrozenCount = membershipService.processExpiredFreezes();
      log.info("Completed scheduled task: Process expired freezes (unfrozen: {})", unfrozenCount);
    } catch (Exception e) {
      log.error("Error processing expired freezes", e);
    }
  }
}
