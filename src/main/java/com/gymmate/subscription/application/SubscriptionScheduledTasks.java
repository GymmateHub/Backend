package com.gymmate.subscription.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduledTasks {

    private final SubscriptionService subscriptionService;
    private final RateLimitService rateLimitService;

    /**
     * Process expired subscriptions every hour
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    @Transactional
    public void processExpiredSubscriptions() {
        log.info("Starting scheduled task: Process expired subscriptions");
        try {
            subscriptionService.processExpiredSubscriptions();
            log.info("Completed scheduled task: Process expired subscriptions");
        } catch (Exception e) {
            log.error("Error processing expired subscriptions", e);
        }
    }

    /**
     * Send renewal notifications daily at 9 AM
     */
    @Scheduled(cron = "0 0 9 * * *") // Daily at 9 AM
    @Transactional
    public void notifyUpcomingRenewals() {
        log.info("Starting scheduled task: Notify upcoming renewals");
        try {
            subscriptionService.notifyUpcomingRenewals();
            log.info("Completed scheduled task: Notify upcoming renewals");
        } catch (Exception e) {
            log.error("Error notifying upcoming renewals", e);
        }
    }

    /**
     * Send trial ending notifications daily at 10 AM
     */
    @Scheduled(cron = "0 0 10 * * *") // Daily at 10 AM
    @Transactional
    public void notifyTrialEndings() {
        log.info("Starting scheduled task: Notify trial endings");
        try {
            subscriptionService.notifyTrialEndings();
            log.info("Completed scheduled task: Notify trial endings");
        } catch (Exception e) {
            log.error("Error notifying trial endings", e);
        }
    }

    /**
     * Clean up old rate limit records daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    @Transactional
    public void cleanupOldRateLimitRecords() {
        log.info("Starting scheduled task: Cleanup old rate limit records");
        try {
            rateLimitService.cleanupOldRecords();
            log.info("Completed scheduled task: Cleanup old rate limit records");
        } catch (Exception e) {
            log.error("Error cleaning up old rate limit records", e);
        }
    }
}

