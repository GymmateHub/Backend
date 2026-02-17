package com.gymmate.shared.security.invite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled task to expire pending invites and cleanup old records.
 *
 * Runs every 15 minutes to:
 * 1. Mark expired pending invites as EXPIRED
 * 2. Delete old completed invites (older than 90 days)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InviteExpirationTask {

    private final UserInviteRepository inviteRepository;

    /**
     * Mark expired pending invites as EXPIRED.
     * Runs every 15 minutes.
     */
    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void expirePendingInvites() {
        LocalDateTime now = LocalDateTime.now();

        int expiredCount = inviteRepository.markExpiredInvites(now);

        if (expiredCount > 0) {
            log.info("Marked {} pending invites as expired", expiredCount);
        } else {
            log.debug("No pending invites to expire");
        }
    }

    /**
     * Delete old completed invites (ACCEPTED, EXPIRED, REVOKED) older than 90 days.
     * Runs daily at 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldInvites() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(90);

        int deletedCount = inviteRepository.deleteOldInvites(cutoff);

        if (deletedCount > 0) {
            log.info("Deleted {} old invites (older than 90 days)", deletedCount);
        } else {
            log.debug("No old invites to cleanup");
        }
    }
}

