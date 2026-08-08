package com.gymmate.subscription.application;

import com.gymmate.payment.application.StripePaymentService;
import com.gymmate.payment.application.port.OrganisationBillingInfoProvider;
import com.gymmate.shared.constants.SubscriptionStatus;
import com.gymmate.subscription.domain.*;
import com.gymmate.subscription.infrastructure.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.gymmate.notification.application.EmailService;
import com.gymmate.notification.application.NotificationService;
import com.gymmate.shared.constants.NotificationPriority;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final OrganisationBillingInfoProvider organisationBillingInfoProvider;

    public Subscription createSubscription(UUID organisationId, String tierName, boolean startTrial) {
        return createSubscription(organisationId, tierName, startTrial, null, true);
    }

    /**
     * Create a subscription with optional Stripe billing integration.
     *
     * @param organisationId      The organisation ID
     * @param tierName            The subscription tier name
     * @param startTrial          Whether to start a trial period
     * @param paymentMethodId     Stripe PaymentMethod ID (pm_xxx) for billing
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
                stripePaymentService.createStripeSubscriptionForOrganisation(organisationId, tier, startTrial);
                log.info("Created Stripe subscription for organisation {} with tier {}", organisationId, tierName);
            } catch (Exception e) {
                log.warn("Failed to create Stripe subscription for organisation {}: {}. " +
                        "Subscription created locally only.", organisationId, e.getMessage());
                // Continue with local subscription - Stripe can be configured later
            }
        }

        log.info("Created subscription for organisation {} with tier {} (trial: {})", organisationId, tierName,
                startTrial);
        return subscription;
    }

    public Subscription getSubscription(UUID organisationId) {
        return SubscriptionRepository.findByOrganisationId(organisationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No subscription found for organisation: " + organisationId));
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
                            subscription.getCurrentMemberCount(), newTier.getMaxMembers()));
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
            throw new IllegalStateException(
                    "Cannot reactivate a cancelled subscription. Please create a new subscription.");
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
            log.warn("Organisation {} should consider upgrading - overage cost exceeds 50% of base cost",
                    organisationId);

            // Send in-app notification
            notificationService.createAndBroadcast(
                    "📈 Consider Upgrading Your Plan",
                    "Your current usage has exceeded your plan limits. Overage charges now exceed 50% of your base plan cost. "
                            + "Upgrading to a higher tier could save you money.",
                    organisationId,
                    NotificationPriority.MEDIUM,
                    "SUBSCRIPTION_UPGRADE_SUGGESTED",
                    Map.of(
                            "currentTier", subscription.getTier().getName(),
                            "memberCount", memberCount,
                            "memberLimit", subscription.getTier().getMaxMembers()
                    ));

            // Send email to organisation contact
            organisationBillingInfoProvider.findBillingInfo(organisationId).ifPresent(org -> {
                String email = org.preferredEmail();
                if (email != null && !email.isBlank()) {
                    emailService.sendHtmlEmail(email,
                            "Consider Upgrading Your GymMate Plan",
                            "<p>Hi " + org.name() + ",</p>"
                                    + "<p>Your current member count (" + memberCount + ") has exceeded your plan limits. "
                                    + "Overage charges now exceed 50% of your base cost.</p>"
                                    + "<p>We recommend upgrading to reduce costs and unlock more features.</p>"
                                    + "<p><a href='http://localhost:3000/gym/settings/subscription'>View Plans</a></p>");
                }
            });
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

    public List<SubscriptionUsage> getOrganisationUsageHistory(UUID organisationId) {
        return usageRepository.findByOrganisationId(organisationId);
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
    @Async
    public void processExpiredSubscriptions() {
        List<Subscription> expiredSubscriptions = SubscriptionRepository
                .findExpiredSubscriptions(LocalDateTime.now(), SubscriptionStatus.ACTIVE);

        for (Subscription subscription : expiredSubscriptions) {
            try {
                subscription.markExpired();
                SubscriptionRepository.save(subscription);

                UUID orgId = subscription.getOrganisationId();
                OrganisationBillingInfoProvider.BillingInfo org = organisationBillingInfoProvider.findBillingInfo(orgId).orElse(null);
                String orgName = org != null ? org.name() : "Your Gym";

                // Notify via SSE
                notificationService.createAndBroadcast(
                        "Subscription Expired",
                        "Your subscription has expired. Please renew to continue using GymMate.",
                        orgId,
                        NotificationPriority.HIGH,
                        "SUBSCRIPTION_EXPIRED",
                        Map.of("subscriptionId", subscription.getId()));

                // Notify via Email
                if (org != null && org.contactEmail() != null) {
                    emailService.sendSubscriptionExpiredEmail(
                            org.contactEmail(),
                            orgName,
                            java.time.LocalDate.now());
                }

                log.info("Marked subscription as expired for organisation {}", orgId);
            } catch (Exception e) {
                log.error("Error processing expired subscription for organisation {}",
                        subscription.getOrganisationId(), e);
            }
        }
    }

    /**
     * Suspend subscriptions that have been PAST_DUE longer than the grace period
     * (Subscription.PAST_DUE_GRACE_PERIOD_DAYS). The failure emails already went out
     * at each webhook-driven payment failure (see AdminNotificationEventListener /
     * PaymentNotificationService) — this is the enforcement step once that grace
     * period has run out with no successful retry.
     */
    @Async
    public void escalatePastDueSubscriptions() {
        LocalDateTime graceCutoff = LocalDateTime.now().minusDays(Subscription.PAST_DUE_GRACE_PERIOD_DAYS);
        List<Subscription> stalePastDue = SubscriptionRepository.findStalePastDueSubscriptions(graceCutoff);

        for (Subscription subscription : stalePastDue) {
            try {
                subscription.suspend();
                SubscriptionRepository.save(subscription);

                UUID orgId = subscription.getOrganisationId();
                notificationService.createAndBroadcast(
                        "Subscription Suspended",
                        "Your subscription has been suspended after a payment failure was not resolved within the grace period. Please update your payment method to restore access.",
                        orgId,
                        NotificationPriority.CRITICAL,
                        "SUBSCRIPTION_SUSPENDED",
                        Map.of("subscriptionId", subscription.getId()));

                log.warn("Suspended subscription {} for organisation {} — past due since {}",
                        subscription.getId(), orgId, subscription.getPastDueSince());
            } catch (Exception e) {
                log.error("Error suspending past-due subscription {}", subscription.getId(), e);
            }
        }
    }

    @Async
    public void notifyUpcomingRenewals() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysFromNow = now.plusDays(3);

        List<Subscription> upcomingRenewals = SubscriptionRepository
                .findSubscriptionsExpiringBetween(now, threeDaysFromNow);

        for (Subscription subscription : upcomingRenewals) {
            try {
                UUID orgId = subscription.getOrganisationId();
                OrganisationBillingInfoProvider.BillingInfo org = organisationBillingInfoProvider.findBillingInfo(orgId).orElse(null);
                String orgName = org != null ? org.name() : "Your Gym";

                // Notify via SSE
                notificationService.createAndBroadcast(
                        "Subscription Renewal",
                        "Your subscription will renew in 3 days.",
                        orgId,
                        NotificationPriority.MEDIUM,
                        "SUBSCRIPTION_RENEWAL",
                        Map.of("subscriptionId", subscription.getId(), "renewalDate",
                                subscription.getCurrentPeriodEnd()));

                // Notify via Email
                if (org != null && org.contactEmail() != null) {
                    emailService.sendSubscriptionRenewalEmail(
                            org.contactEmail(),
                            orgName,
                            subscription.getTier().getName(),
                            subscription.getCurrentPeriodEnd().toLocalDate(),
                            subscription.getTier().getPrice());
                }

                log.info("Notified organisation {} of upcoming renewal", orgId);
            } catch (Exception e) {
                log.error("Error notifying renewal for organisation {}", subscription.getOrganisationId(), e);
            }
        }
    }

    @Async
    public void notifyTrialEndings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysFromNow = now.plusDays(2);

        List<Subscription> endingTrials = SubscriptionRepository
                .findTrialsEndingBetween(now, twoDaysFromNow);

        for (Subscription subscription : endingTrials) {
            try {
                UUID orgId = subscription.getOrganisationId();
                OrganisationBillingInfoProvider.BillingInfo org = organisationBillingInfoProvider.findBillingInfo(orgId).orElse(null);
                String orgName = org != null ? org.name() : "Your Gym";

                // Notify via SSE
                notificationService.createAndBroadcast(
                        "Trial Ending Soon",
                        "Your free trial is ending in 2 days. Upgrade now to keep using GymMate.",
                        orgId,
                        NotificationPriority.HIGH,
                        "TRIAL_ENDING",
                        Map.of("subscriptionId", subscription.getId(), "trialEnd", subscription.getTrialEnd()));

                // Notify via Email
                if (org != null && org.contactEmail() != null) {
                    emailService.sendTrialEndingEmail(
                            org.contactEmail(),
                            orgName,
                            subscription.getTrialEnd().toLocalDate());
                }

                log.info("Notified organisation {} of trial ending", orgId);
            } catch (Exception e) {
                log.error("Error notifying trial ending for organisation {}", subscription.getOrganisationId(), e);
            }
        }
    }
}
