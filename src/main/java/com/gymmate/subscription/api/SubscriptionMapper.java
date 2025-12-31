package com.gymmate.subscription.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.subscription.api.dto.SubscriptionResponse;
import com.gymmate.subscription.api.dto.SubscriptionTierResponse;
import com.gymmate.subscription.domain.Subscription;
import com.gymmate.subscription.domain.SubscriptionTier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class SubscriptionMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SubscriptionResponse toResponse(Subscription subscription) {
        LocalDateTime now = LocalDateTime.now();

        // Calculate days remaining in trial
        Long daysRemainingInTrial = null;
        if (subscription.isInTrial() && subscription.getTrialEnd() != null) {
            daysRemainingInTrial = ChronoUnit.DAYS.between(now, subscription.getTrialEnd());
            if (daysRemainingInTrial < 0) daysRemainingInTrial = 0L;
        }

        // Calculate days until renewal
        Long daysUntilRenewal = null;
        if (subscription.getCurrentPeriodEnd() != null) {
            daysUntilRenewal = ChronoUnit.DAYS.between(now, subscription.getCurrentPeriodEnd());
            if (daysUntilRenewal < 0) daysUntilRenewal = 0L;
        }

        return SubscriptionResponse.builder()
            .id(subscription.getId())
            .organisationId(subscription.getOrganisationId())
            .tierName(subscription.getTier().getName())
            .tierDisplayName(subscription.getTier().getDisplayName())
            .status(subscription.getStatus().name())
            .currentPeriodStart(subscription.getCurrentPeriodStart())
            .currentPeriodEnd(subscription.getCurrentPeriodEnd())
            .cancelAtPeriodEnd(subscription.getCancelAtPeriodEnd())
            .cancelledAt(subscription.getCancelledAt())
            .trialStart(subscription.getTrialStart())
            .trialEnd(subscription.getTrialEnd())
            .currentMemberCount(subscription.getCurrentMemberCount())
            .maxMembers(subscription.getTier().getMaxMembers())
            .price(subscription.getTier().getPrice())
            .billingCycle(subscription.getTier().getBillingCycle())
            .apiRequestsPerHour(subscription.getTier().getApiRequestsPerHour())
            .apiBurstLimit(subscription.getTier().getApiBurstLimit())
            .smsCreditsPerMonth(subscription.getTier().getSmsCreditsPerMonth())
            .emailCreditsPerMonth(subscription.getTier().getEmailCreditsPerMonth())
            .isActive(subscription.isActive())
            .isInTrial(subscription.isInTrial())
            .hasExceededMemberLimit(subscription.hasExceededMemberLimit())
            .memberOverage(subscription.getMemberOverage())
            .hasStripeSubscription(subscription.getStripeSubscriptionId() != null)
            .hasPaymentMethod(subscription.getStripeCustomerId() != null)
            .daysRemainingInTrial(daysRemainingInTrial)
            .daysUntilRenewal(daysUntilRenewal)
            .build();
    }

    public SubscriptionTierResponse toTierResponse(SubscriptionTier tier) {
        List<String> features = parseFeatures(tier.getFeatures());

        return SubscriptionTierResponse.builder()
            .id(tier.getId())
            .name(tier.getName())
            .displayName(tier.getDisplayName())
            .description(tier.getDescription())
            .price(tier.getPrice())
            .billingCycle(tier.getBillingCycle())
            .isActive(tier.getActive())
            .isFeatured(tier.getFeatured())
            .maxMembers(tier.getMaxMembers())
            .maxLocations(tier.getMaxLocations())
            .maxStaff(tier.getMaxStaff())
            .maxClassesPerMonth(tier.getMaxClassesPerMonth())
            .apiRequestsPerHour(tier.getApiRequestsPerHour())
            .apiBurstLimit(tier.getApiBurstLimit())
            .concurrentConnections(tier.getConcurrentConnections())
            .smsCreditsPerMonth(tier.getSmsCreditsPerMonth())
            .emailCreditsPerMonth(tier.getEmailCreditsPerMonth())
            .features(features)
            .overageMemberPrice(tier.getOverageMemberPrice())
            .overageSmsPrice(tier.getOverageSmsPrice())
            .overageEmailPrice(tier.getOverageEmailPrice())
            .sortOrder(tier.getSortOrder())
            .trialDays(tier.getTrialDays())
            .hasStripeIntegration(tier.getStripePriceId() != null && !tier.getStripePriceId().isBlank())
            .build();
    }

    private List<String> parseFeatures(String featuresJson) {
        if (featuresJson == null || featuresJson.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(featuresJson, new TypeReference<>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}

