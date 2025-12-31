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

    private final SubscriptionRepository SubscriptionRepository;
    private final SubscriptionTierRepository tierRepository;
    private final SubscriptionUsageRepository usageRepository;
    private final StripePaymentService stripePaymentService;

    public Subscription createSubscription(UUID organisationId, String tierName, boolean startTrial) {
        return createSubscription(organisationId, tierName, startTrial, null, true);
    }

    /**
     * Create a subscription with optional Stripe billing integration.
     *
     * @param organisationId     The organisation ID
     * @param tierName           The subscription tier name
     * @param startTrial         Whether to start a trial period
     * @param paymentMethodId    Stripe PaymentMethod ID (pm_xxx) for billing
     * @param enableStripeBilling Whether to create subscription in Stripe
     * @return The created subscription
     */
    public Subscription createSubscription(UUID organisationId, String tierName, boolean startTrial,
                                               String paymentMethodId, boolean enableStripeBilling) {
        // Check if organisation already has a subscription
        SubscriptionRepository.findByOrganisationId(organisationId)
            .ifPresent(existing -> {
                throw new IllegalStateException("Organisation already has an active subscription");
            });

        SubscriptionTier tier = tierRepository.findByName(tierName)
            .orElseThrow(() -> new IllegalArgumentException("Subscription tier not found: " + tierName));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodEnd = now.plusMonths(1);

        Subscription subscription = Subscription.builder()
            .organisationId(organisationId)
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

        subscription = SubscriptionRepository.save(subscription);

        // Create initial usage record
        createUsageRecord(subscription);

        // Integrate with Stripe if enabled and tier has Stripe pricing configured
        if (enableStripeBilling && tier.getStripePriceId() != null && !tier.getStripePriceId().isBlank()) {
            try {
                // Attach payment method if provided
                if (paymentMethodId != null && !paymentMethodId.isBlank()) {
                    stripePaymentService.attachPaymentMethod(organisationId, paymentMethodId, true);
                }

                // Create Stripe subscription
                stripePaymentService.createStripeSubscription(organisationId, tier, startTrial);
                log.info("Created Stripe subscription for organisation {} with tier {}", organisationId, tierName);
            } catch (Exception e) {
                log.warn("Failed to create Stripe subscription for organisation {}: {}. " +
                         "Subscription created locally only.", organisationId, e.getMessage());
                // Continue with local subscription - Stripe can be configured later
            }
        }

        log.info("Created subscription for organisation {} with tier {} (trial: {})", organisationId, tierName, startTrial);
        return subscription;
    }

    public Subscription getSubscription(UUID organisationId) {
        return SubscriptionRepository.findByOrganisationId(organisationId)
            .orElseThrow(() -> new IllegalArgumentException("No subscription found for organisation: " + organisationId));
    }

    public Subscription upgradeSubscription(UUID organisationId, String newTierName) {
        Subscription subscription = getSubscription(organisationId);
        SubscriptionTier newTier = tierRepository.findByName(newTierName)
            .orElseThrow(() -> new IllegalArgumentException("Subscription tier not found: " + newTierName));

        if (subscription.getTier().getPrice().compareTo(newTier.getPrice()) >= 0) {
            throw new IllegalArgumentException("New tier must be a higher tier");
        }

        subscription.upgradeTier(newTier);
        SubscriptionRepository.save(subscription);

        log.info("Upgraded subscription for organisation {} to tier {}", organisationId, newTierName);
        return subscription;
    }

    public Subscription downgradeSubscription(UUID organisationId, String newTierName) {
        Subscription subscription = getSubscription(organisationId);
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
        SubscriptionRepository.save(subscription);

        log.info("Downgraded subscription for organisation {} to tier {}", organisationId, newTierName);
        return subscription;
    }

    public Subscription cancelSubscription(UUID organisationId, boolean immediate) {
        Subscription subscription = getSubscription(organisationId);

        if (immediate) {
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscription.setCancelledAt(LocalDateTime.now());
        } else {
            subscription.cancelAtPeriodEnd();
        }

        SubscriptionRepository.save(subscription);

        log.info("Cancelled subscription for organisation {} (immediate: {})", organisationId, immediate);
        return subscription;
    }

    public Subscription reactivateSubscription(UUID organisationId) {
        Subscription subscription = getSubscription(organisationId);

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot reactivate a cancelled subscription. Please create a new subscription.");
        }

        subscription.reactivate();
        subscription.activate();
        SubscriptionRepository.save(subscription);

        log.info("Reactivated subscription for organisation {}", organisationId);
        return subscription;
    }

    public void updateMemberCount(UUID organisationId, Integer memberCount) {
        Subscription subscription = getSubscription(organisationId);
        subscription.updateMemberCount(memberCount);
        SubscriptionRepository.save(subscription);

        // Update current usage record
        SubscriptionUsage usage = getCurrentUsage(subscription.getId());
        usage.updateMemberCount(memberCount);
        usageRepository.save(usage);

        // Check if upgrade notification is needed
        if (usage.needsUpgradeNotification()) {
            log.warn("Organisation {} should consider upgrading - overage cost exceeds 50% of base cost", organisationId);
            // TODO: Send notification
        }
    }

    public void renewSubscription(UUID organisationId) {
        Subscription subscription = getSubscription(organisationId);

        LocalDateTime newStart = subscription.getCurrentPeriodEnd();
        LocalDateTime newEnd = newStart.plusMonths(1);

        subscription.renewPeriod(newStart, newEnd);

        if (subscription.getCancelAtPeriodEnd()) {
            subscription.setStatus(SubscriptionStatus.CANCELLED);
        }

        SubscriptionRepository.save(subscription);

        // Create new usage record for new period
        createUsageRecord(subscription);

        log.info("Renewed subscription for organisation {}", organisationId);
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

    private void createUsageRecord(Subscription subscription) {
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
        List<Subscription> expiredSubscriptions = SubscriptionRepository
            .findExpiredSubscriptions(LocalDateTime.now(), SubscriptionStatus.ACTIVE);

        for (Subscription subscription : expiredSubscriptions) {
            subscription.markExpired();
            SubscriptionRepository.save(subscription);
            log.info("Marked subscription as expired for organisation {}", subscription.getOrganisationId());
        }
    }

    public void notifyUpcomingRenewals() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysFromNow = now.plusDays(3);

        List<Subscription> upcomingRenewals = SubscriptionRepository
            .findSubscriptionsExpiringBetween(now, threeDaysFromNow);

        for (Subscription subscription : upcomingRenewals) {
            log.info("Subscription renewal upcoming for organisation {} in 3 days", subscription.getOrganisationId());
            // TODO: Send notification
        }
    }

    public void notifyTrialEndings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysFromNow = now.plusDays(2);

        List<Subscription> endingTrials = SubscriptionRepository
            .findTrialsEndingBetween(now, twoDaysFromNow);

        for (Subscription subscription : endingTrials) {
            log.info("Trial ending soon for organisation {} in 2 days", subscription.getOrganisationId());
            // TODO: Send notification
        }
    }
}

