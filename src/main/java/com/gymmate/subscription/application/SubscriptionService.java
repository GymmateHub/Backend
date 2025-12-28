package com.gymmate.subscription.application;

import com.gymmate.payment.application.StripePaymentService;
import com.gymmate.subscription.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionService {

    private final GymSubscriptionRepository gymSubscriptionRepository;
    private final SubscriptionTierRepository tierRepository;
    private final SubscriptionUsageRepository usageRepository;
    private final StripePaymentService stripePaymentService;

    public GymSubscription createSubscription(UUID gymId, String tierName, boolean startTrial) {
        return createSubscription(gymId, tierName, startTrial, null, true);
    }

    /**
     * Create a subscription with optional Stripe billing integration.
     *
     * @param gymId              The gym ID
     * @param tierName           The subscription tier name
     * @param startTrial         Whether to start a trial period
     * @param paymentMethodId    Stripe PaymentMethod ID (pm_xxx) for billing
     * @param enableStripeBilling Whether to create subscription in Stripe
     * @return The created subscription
     */
    public GymSubscription createSubscription(UUID gymId, String tierName, boolean startTrial,
                                               String paymentMethodId, boolean enableStripeBilling) {
        // Check if gym already has a subscription
        gymSubscriptionRepository.findByGymId(gymId)
            .ifPresent(existing -> {
                throw new IllegalStateException("Gym already has an active subscription");
            });

        SubscriptionTier tier = tierRepository.findByName(tierName)
            .orElseThrow(() -> new IllegalArgumentException("Subscription tier not found: " + tierName));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodEnd = now.plusMonths(1);

        GymSubscription subscription = GymSubscription.builder()
            .gymId(gymId)
            .tier(tier)
            .status(startTrial ? SubscriptionStatus.TRIAL : SubscriptionStatus.ACTIVE)
            .currentPeriodStart(now)
            .currentPeriodEnd(periodEnd)
            .currentMemberCount(0)
            .currentLocationCount(1)
            .build();

        if (startTrial) {
            int trialDays = tier.getTrialDays() != null ? tier.getTrialDays() : 14;
            subscription.setTrialStart(now);
            subscription.setTrialEnd(now.plusDays(trialDays));
            subscription.setCurrentPeriodEnd(now.plusDays(trialDays)); // Trial period end
        }

        subscription = gymSubscriptionRepository.save(subscription);

        // Create initial usage record
        createUsageRecord(subscription);

        // Integrate with Stripe if enabled and tier has Stripe pricing configured
        if (enableStripeBilling && tier.getStripePriceId() != null && !tier.getStripePriceId().isBlank()) {
            try {
                // Attach payment method if provided
                if (paymentMethodId != null && !paymentMethodId.isBlank()) {
                    stripePaymentService.attachPaymentMethod(gymId, paymentMethodId, true);
                }

                // Create Stripe subscription
                stripePaymentService.createStripeSubscription(gymId, tier, startTrial);
                log.info("Created Stripe subscription for gym {} with tier {}", gymId, tierName);
            } catch (Exception e) {
                log.warn("Failed to create Stripe subscription for gym {}: {}. " +
                         "Subscription created locally only.", gymId, e.getMessage());
                // Continue with local subscription - Stripe can be configured later
            }
        }

        log.info("Created subscription for gym {} with tier {} (trial: {})", gymId, tierName, startTrial);
        return subscription;
    }

    public GymSubscription getGymSubscription(UUID gymId) {
        return gymSubscriptionRepository.findByGymId(gymId)
            .orElseThrow(() -> new IllegalArgumentException("No subscription found for gym: " + gymId));
    }

    public GymSubscription upgradeSubscription(UUID gymId, String newTierName) {
        GymSubscription subscription = getGymSubscription(gymId);
        SubscriptionTier newTier = tierRepository.findByName(newTierName)
            .orElseThrow(() -> new IllegalArgumentException("Subscription tier not found: " + newTierName));

        if (subscription.getTier().getPrice().compareTo(newTier.getPrice()) >= 0) {
            throw new IllegalArgumentException("New tier must be a higher tier");
        }

        subscription.upgradeTier(newTier);
        gymSubscriptionRepository.save(subscription);

        log.info("Upgraded subscription for gym {} to tier {}", gymId, newTierName);
        return subscription;
    }

    public GymSubscription downgradeSubscription(UUID gymId, String newTierName) {
        GymSubscription subscription = getGymSubscription(gymId);
        SubscriptionTier newTier = tierRepository.findByName(newTierName)
            .orElseThrow(() -> new IllegalArgumentException("Subscription tier not found: " + newTierName));

        if (subscription.getTier().getPrice().compareTo(newTier.getPrice()) <= 0) {
            throw new IllegalArgumentException("New tier must be a lower tier");
        }

        // Check if current usage fits in new tier
        if (subscription.getCurrentMemberCount() > newTier.getMaxMembers()) {
            throw new IllegalArgumentException(
                String.format("Cannot downgrade: Current member count (%d) exceeds new tier limit (%d)",
                    subscription.getCurrentMemberCount(), newTier.getMaxMembers())
            );
        }

        subscription.upgradeTier(newTier);
        gymSubscriptionRepository.save(subscription);

        log.info("Downgraded subscription for gym {} to tier {}", gymId, newTierName);
        return subscription;
    }

    public GymSubscription cancelSubscription(UUID gymId, boolean immediate) {
        GymSubscription subscription = getGymSubscription(gymId);

        if (immediate) {
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscription.setCancelledAt(LocalDateTime.now());
        } else {
            subscription.cancelAtPeriodEnd();
        }

        gymSubscriptionRepository.save(subscription);

        log.info("Cancelled subscription for gym {} (immediate: {})", gymId, immediate);
        return subscription;
    }

    public GymSubscription reactivateSubscription(UUID gymId) {
        GymSubscription subscription = getGymSubscription(gymId);

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot reactivate a cancelled subscription. Please create a new subscription.");
        }

        subscription.reactivate();
        subscription.activate();
        gymSubscriptionRepository.save(subscription);

        log.info("Reactivated subscription for gym {}", gymId);
        return subscription;
    }

    public void updateMemberCount(UUID gymId, Integer memberCount) {
        GymSubscription subscription = getGymSubscription(gymId);
        subscription.updateMemberCount(memberCount);
        gymSubscriptionRepository.save(subscription);

        // Update current usage record
        SubscriptionUsage usage = getCurrentUsage(subscription.getId());
        usage.updateMemberCount(memberCount);
        usageRepository.save(usage);

        // Check if upgrade notification is needed
        if (usage.needsUpgradeNotification()) {
            log.warn("Gym {} should consider upgrading - overage cost exceeds 50% of base cost", gymId);
            // TODO: Send notification
        }
    }

    public void renewSubscription(UUID gymId) {
        GymSubscription subscription = getGymSubscription(gymId);

        LocalDateTime newStart = subscription.getCurrentPeriodEnd();
        LocalDateTime newEnd = newStart.plusMonths(1);

        subscription.renewPeriod(newStart, newEnd);

        if (subscription.getCancelAtPeriodEnd()) {
            subscription.setStatus(SubscriptionStatus.CANCELLED);
        }

        gymSubscriptionRepository.save(subscription);

        // Create new usage record for new period
        createUsageRecord(subscription);

        log.info("Renewed subscription for gym {}", gymId);
    }

    public List<SubscriptionTier> getAllActiveTiers() {
        return tierRepository.findByActiveTrueOrderBySortOrder();
    }

    public List<SubscriptionTier> getFeaturedTiers() {
        return tierRepository.findFeaturedTiers();
    }

    public SubscriptionUsage getCurrentUsage(UUID subscriptionId) {
        LocalDateTime now = LocalDateTime.now();
        return usageRepository.findBySubscriptionAndPeriod(subscriptionId, now)
            .orElseThrow(() -> new IllegalStateException("No current usage record found"));
    }

    public List<SubscriptionUsage> getGymUsageHistory(UUID gymId) {
        return usageRepository.findByGymId(gymId);
    }

    private void createUsageRecord(GymSubscription subscription) {
        SubscriptionUsage usage = SubscriptionUsage.builder()
            .subscription(subscription)
            .billingPeriodStart(subscription.getCurrentPeriodStart())
            .billingPeriodEnd(subscription.getCurrentPeriodEnd())
            .memberCount(subscription.getCurrentMemberCount())
            .baseCost(subscription.getTier().getPrice())
            .totalCost(subscription.getTier().getPrice())
            .build();

        usageRepository.save(usage);
    }

    // Background job methods
    public void processExpiredSubscriptions() {
        List<GymSubscription> expiredSubscriptions = gymSubscriptionRepository
            .findExpiredSubscriptions(LocalDateTime.now(), SubscriptionStatus.ACTIVE);

        for (GymSubscription subscription : expiredSubscriptions) {
            subscription.markExpired();
            gymSubscriptionRepository.save(subscription);
            log.info("Marked subscription as expired for gym {}", subscription.getGymId());
        }
    }

    public void notifyUpcomingRenewals() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysFromNow = now.plusDays(3);

        List<GymSubscription> upcomingRenewals = gymSubscriptionRepository
            .findSubscriptionsExpiringBetween(now, threeDaysFromNow);

        for (GymSubscription subscription : upcomingRenewals) {
            log.info("Subscription renewal upcoming for gym {} in 3 days", subscription.getGymId());
            // TODO: Send notification
        }
    }

    public void notifyTrialEndings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysFromNow = now.plusDays(2);

        List<GymSubscription> endingTrials = gymSubscriptionRepository
            .findTrialsEndingBetween(now, twoDaysFromNow);

        for (GymSubscription subscription : endingTrials) {
            log.info("Trial ending soon for gym {} in 2 days", subscription.getGymId());
            // TODO: Send notification
        }
    }
}

