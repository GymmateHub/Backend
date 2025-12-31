package com.gymmate.subscription.application;

import com.gymmate.subscription.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RateLimitService {

    private final ApiRateLimitRepository rateLimitRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionUsageRepository usageRepository;

    /**
     * Check if an organisation can make an API request based on their rate limits
     * @return true if allowed, false if rate limited
     */
    public boolean checkRateLimit(UUID organisationId, String windowType, String endpoint, String ipAddress) {
        Subscription subscription = subscriptionRepository.findByOrganisationId(organisationId)
            .orElseThrow(() -> new IllegalArgumentException("No subscription found for organisation: " + organisationId));

        if (!subscription.canAccess()) {
            log.warn("Organisation {} subscription is not active, blocking request", organisationId);
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = calculateWindowStart(now, windowType);
        LocalDateTime windowEnd = calculateWindowEnd(windowStart, windowType);

        ApiRateLimit rateLimit = rateLimitRepository
            .findCurrentWindow(organisationId, now, windowType)
            .orElseGet(() -> createNewWindow(organisationId, windowStart, windowEnd, windowType, subscription.getTier()));

        // Check if currently blocked
        if (rateLimit.isCurrentlyBlocked()) {
            log.warn("Organisation {} is currently rate limited until {}", organisationId, rateLimit.getBlockedUntil());
            return false;
        }

        // Check if window has expired, create new one
        if (rateLimit.isExpired()) {
            rateLimit = createNewWindow(organisationId, windowStart, windowEnd, windowType, subscription.getTier());
        }

        // Increment request count
        rateLimit.incrementRequest();
        rateLimit.setEndpointPath(endpoint);
        rateLimit.setIpAddress(ipAddress);
        rateLimitRepository.save(rateLimit);

        // Update usage tracking
        updateUsageTracking(subscription.getId());

        // If blocked, record the hit
        if (rateLimit.isCurrentlyBlocked()) {
            recordRateLimitHit(subscription.getId());
            log.warn("Organisation {} has been rate limited. Requests: {}/{}",
                organisationId, rateLimit.getRequestCount(), rateLimit.getLimitThreshold());
            return false;
        }

        return true;
    }

    /**
     * Get current rate limit status for an organisation
     */
    public RateLimitStatus getRateLimitStatus(UUID organisationId) {
        Subscription subscription = subscriptionRepository.findByOrganisationId(organisationId)
            .orElseThrow(() -> new IllegalArgumentException("No subscription found for organisation: " + organisationId));

        LocalDateTime now = LocalDateTime.now();

        ApiRateLimit hourlyLimit = rateLimitRepository
            .findCurrentWindow(organisationId, now, "hourly")
            .orElse(null);

        ApiRateLimit burstLimit = rateLimitRepository
            .findCurrentWindow(organisationId, now, "burst")
            .orElse(null);

        return RateLimitStatus.builder()
            .gymId(organisationId) // Note: field name in DTO might need updating
            .tierName(subscription.getTier().getName())
            .hourlyLimit(subscription.getTier().getApiRequestsPerHour())
            .hourlyRemaining(hourlyLimit != null ? hourlyLimit.getRemainingRequests() : subscription.getTier().getApiRequestsPerHour())
            .hourlyUsed(hourlyLimit != null ? hourlyLimit.getRequestCount() : 0)
            .burstLimit(subscription.getTier().getApiBurstLimit())
            .burstRemaining(burstLimit != null ? burstLimit.getRemainingRequests() : subscription.getTier().getApiBurstLimit())
            .burstUsed(burstLimit != null ? burstLimit.getRequestCount() : 0)
            .isBlocked(hourlyLimit != null && hourlyLimit.isCurrentlyBlocked())
            .blockedUntil(hourlyLimit != null ? hourlyLimit.getBlockedUntil() : null)
            .build();
    }

    /**
     * Manually unblock an organisation (admin action)
     */
    public void unblockOrganisation(UUID organisationId) {
        LocalDateTime now = LocalDateTime.now();
        var activeBlocks = rateLimitRepository.findActiveBlocks(organisationId, now);

        for (ApiRateLimit block : activeBlocks) {
            block.unblock();
            rateLimitRepository.save(block);
        }

        log.info("Manually unblocked organisation {}", organisationId);
    }

    /**
     * Get rate limit statistics for an organisation
     */
    public RateLimitStatistics getStatistics(UUID organisationId, LocalDateTime since) {
        Long blockCount = rateLimitRepository.countBlocksSince(organisationId, since);

        Subscription subscription = subscriptionRepository.findByOrganisationId(organisationId)
            .orElseThrow(() -> new IllegalArgumentException("No subscription found for organisation: " + organisationId));

        var currentUsage = usageRepository
            .findBySubscriptionAndPeriod(subscription.getId(), LocalDateTime.now())
            .orElse(null);

        return RateLimitStatistics.builder()
            .gymId(organisationId) // Note: field name in DTO might need updating
            .totalRequests(currentUsage != null ? currentUsage.getApiRequests() : 0)
            .totalBlocks(blockCount.intValue())
            .rateLimitHits(currentUsage != null ? currentUsage.getApiRateLimitHits() : 0)
            .since(since)
            .build();
    }

    /**
     * Clean up old rate limit records
     */
    @Transactional
    public void cleanupOldRecords() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        rateLimitRepository.deleteByWindowEndBefore(cutoffDate);
        log.info("Cleaned up rate limit records older than {}", cutoffDate);
    }

    private ApiRateLimit createNewWindow(UUID organisationId, LocalDateTime windowStart,
                                        LocalDateTime windowEnd, String windowType,
                                        SubscriptionTier tier) {
        Integer threshold = "burst".equals(windowType)
            ? tier.getApiBurstLimit()
            : tier.getApiRequestsPerHour();

        ApiRateLimit rateLimit = ApiRateLimit.builder()
            .organisationId(organisationId)
            .windowStart(windowStart)
            .windowEnd(windowEnd)
            .windowType(windowType)
            .limitThreshold(threshold)
            .requestCount(0)
            .build();

        return rateLimitRepository.save(rateLimit);
    }

    private LocalDateTime calculateWindowStart(LocalDateTime now, String windowType) {
        return switch (windowType) {
            case "hourly" -> now.truncatedTo(ChronoUnit.HOURS);
            case "burst" -> now.truncatedTo(ChronoUnit.MINUTES);
            case "daily" -> now.truncatedTo(ChronoUnit.DAYS);
            default -> now.truncatedTo(ChronoUnit.HOURS);
        };
    }

    private LocalDateTime calculateWindowEnd(LocalDateTime windowStart, String windowType) {
        return switch (windowType) {
            case "hourly" -> windowStart.plusHours(1);
            case "burst" -> windowStart.plusMinutes(1);
            case "daily" -> windowStart.plusDays(1);
            default -> windowStart.plusHours(1);
        };
    }

    private void updateUsageTracking(UUID subscriptionId) {
        SubscriptionUsage usage = usageRepository
            .findBySubscriptionAndPeriod(subscriptionId, LocalDateTime.now())
            .orElseThrow(() -> new IllegalStateException("No current usage record found"));

        usage.incrementApiRequest();
        usageRepository.save(usage);
    }

    private void recordRateLimitHit(UUID subscriptionId) {
        SubscriptionUsage usage = usageRepository
            .findBySubscriptionAndPeriod(subscriptionId, LocalDateTime.now())
            .orElseThrow(() -> new IllegalStateException("No current usage record found"));

        usage.recordRateLimitHit();
        usageRepository.save(usage);
    }
}

