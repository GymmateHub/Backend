package com.gymmate.billing.internal.application.mapper;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.gymmate.billing.internal.application.dto.SubscriptionResponse;
import com.gymmate.billing.internal.application.dto.SubscriptionTierResponse;
import com.gymmate.billing.internal.domain.Subscription;
import com.gymmate.billing.internal.domain.SubscriptionTier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class SubscriptionMapper {

    private final ObjectMapper objectMapper = new JsonMapper();

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

        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getOrganisationId(),
                subscription.getTier().getName(),
                subscription.getTier().getDisplayName(),
                subscription.getStatus().name(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.getCancelAtPeriodEnd(),
                subscription.getCancelledAt(),
                subscription.getTrialStart(),
                subscription.getTrialEnd(),
                subscription.getCurrentMemberCount(),
                subscription.getTier().getMaxMembers(),
                subscription.getTier().getPrice(),
                subscription.getTier().getBillingCycle(),
                subscription.getTier().getApiRequestsPerHour(),
                subscription.getTier().getApiBurstLimit(),
                subscription.getTier().getSmsCreditsPerMonth(),
                subscription.getTier().getEmailCreditsPerMonth(),
                subscription.isActive(),
                subscription.isInTrial(),
                subscription.hasExceededMemberLimit(),
                subscription.getMemberOverage(),
                subscription.getStripeSubscriptionId() != null,
                subscription.getStripeCustomerId() != null,
                daysRemainingInTrial,
                daysUntilRenewal);
    }

    public SubscriptionTierResponse toTierResponse(SubscriptionTier tier) {
        List<String> features = parseFeatures(tier.getFeatures());

        return new SubscriptionTierResponse(
                tier.getId(),
                tier.getName(),
                tier.getDisplayName(),
                tier.getDescription(),
                tier.getPrice(),
                tier.getBillingCycle(),
                tier.getActive(),
                tier.getFeatured(),
                tier.getMaxMembers(),
                tier.getMaxLocations(),
                tier.getMaxStaff(),
                tier.getMaxClassesPerMonth(),
                tier.getApiRequestsPerHour(),
                tier.getApiBurstLimit(),
                tier.getConcurrentConnections(),
                tier.getSmsCreditsPerMonth(),
                tier.getEmailCreditsPerMonth(),
                features,
                tier.getOverageMemberPrice(),
                tier.getOverageSmsPrice(),
                tier.getOverageEmailPrice(),
                tier.getSortOrder(),
                tier.getTrialDays(),
                tier.getStripePriceId() != null && !tier.getStripePriceId().isBlank());
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

