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
   * Expire memberships that have passed their end date and are not set to auto-renew.
   * Runs daily at midnight.
   */
  @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
  @Transactional
  public void processExpiredMemberships() {
    log.info("Starting scheduled task: Process expired memberships");
    try {
      int expiredCount = membershipService.processExpiredMemberships();
      log.info("Completed scheduled task: Process expired memberships (expired: {})", expiredCount);
    } catch (Exception e) {
      log.error("Error processing expired memberships", e);
    }
  }

  /**
   * Auto-renew memberships that have passed their end date and have auto-renew enabled.
   * Runs daily at 6 AM (after expiry check, before business hours).
   */
  @Scheduled(cron = "0 0 6 * * *") // Daily at 6 AM
  @Transactional
  public void processAutoRenewals() {
    log.info("Starting scheduled task: Process auto-renewals");
    try {
      int renewedCount = membershipService.processAutoRenewals();
      log.info("Completed scheduled task: Process auto-renewals (renewed: {})", renewedCount);
    } catch (Exception e) {
      log.error("Error processing auto-renewals", e);
    }
  }

  /**
   * Suspend memberships past due beyond the grace period. Runs hourly, matching the
   * subscription-side escalation cadence (SubscriptionScheduledTasks).
   */
  @Scheduled(cron = "0 30 * * * *") // Every hour at minute 30
  @Transactional
  public void escalatePastDueMemberships() {
    log.info("Starting scheduled task: Escalate past-due memberships");
    try {
      int suspendedCount = membershipService.escalatePastDueMemberships();
      log.info("Completed scheduled task: Escalate past-due memberships (suspended: {})", suspendedCount);
    } catch (Exception e) {
      log.error("Error escalating past-due memberships", e);
    }
  }

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
