package com.gymmate.shared.security.task;

import com.gymmate.shared.security.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Scheduled task to clean up expired tokens from the blacklist.
 * Runs daily at 2 AM.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenBlacklistCleanupTask {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting token blacklist cleanup task");
        try {
            Date now = new Date();
            long expiredCount = tokenBlacklistRepository.countExpiredTokens(now);

            if (expiredCount > 0) {
                log.info("Found {} expired tokens to clean up", expiredCount);
                tokenBlacklistRepository.deleteExpiredTokens(now);
                log.info("Successfully deleted {} expired tokens from blacklist", expiredCount);
            } else {
                log.debug("No expired tokens found in blacklist");
            }
        } catch (Exception e) {
            log.error("Error during token blacklist cleanup: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void cleanupNow() {
        log.info("Manual token blacklist cleanup triggered");
        cleanupExpiredTokens();
    }
}
