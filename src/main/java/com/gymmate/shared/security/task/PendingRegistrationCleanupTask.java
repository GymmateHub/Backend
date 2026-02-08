package com.gymmate.shared.security.task;

import com.gymmate.shared.domain.PendingRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Scheduled task to clean up expired pending registrations.
 * Runs every hour.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PendingRegistrationCleanupTask {

    private final PendingRegistrationRepository pendingRegistrationRepository;

    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void cleanupExpiredRegistrations() {
        log.info("Starting cleanup of expired pending registrations");
        try {
            pendingRegistrationRepository.deleteByExpiresAtBefore(Instant.now());
            log.info("Successfully cleaned up expired pending registrations");
        } catch (Exception e) {
            log.error("Failed to cleanup expired pending registrations", e);
        }
    }
}
