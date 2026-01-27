package com.gymmate.notification.application;

import com.gymmate.notification.api.dto.AudiencePreviewResponse;
import com.gymmate.notification.api.dto.CreateCampaignRequest;
import com.gymmate.notification.domain.*;
import com.gymmate.notification.infrastructure.CampaignRecipientRepository;
import com.gymmate.notification.infrastructure.NewsletterCampaignRepository;
import com.gymmate.notification.infrastructure.NewsletterTemplateRepository;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing newsletter campaigns.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NewsletterCampaignService {

    private final NewsletterCampaignRepository campaignRepository;
    private final NewsletterTemplateRepository templateRepository;
    private final CampaignRecipientRepository recipientRepository;
    private final AudienceResolver audienceResolver;
    private final NewsletterTemplateService templateService;
    private final EmailService emailService;

    /**
     * Create a new campaign.
     */
    @Transactional
    public NewsletterCampaign create(CreateCampaignRequest request, UUID createdBy) {
        String subject = request.getSubject();
        String body = request.getBody();

        // If using a template, copy content from template
        if (request.getTemplateId() != null) {
            NewsletterTemplate template = templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new DomainException("TEMPLATE_NOT_FOUND",
                            "Template not found: " + request.getTemplateId()));
            if (subject == null || subject.isBlank()) {
                subject = template.getSubject();
            }
            if (body == null || body.isBlank()) {
                body = template.getBody();
            }
        }

        NewsletterCampaign campaign = NewsletterCampaign.builder()
                .templateId(request.getTemplateId())
                .name(request.getName())
                .subject(subject)
                .body(body)
                .audienceType(request.getAudienceType())
                .audienceFilter(request.getAudienceFilter())
                .build();
        campaign.setCreatedBy(createdBy.toString());

        campaign.setGymId(request.getGymId());
        campaign.setOrganisationId(TenantContext.getCurrentTenantId());

        // Schedule if requested
        if (request.getScheduledAt() != null) {
            campaign.schedule(request.getScheduledAt());
        }

        NewsletterCampaign saved = campaignRepository.save(campaign);
        log.info("Created newsletter campaign: {} for gym: {}", saved.getId(), request.getGymId());
        return saved;
    }

    /**
     * Get campaign by ID.
     */
    @Transactional(readOnly = true)
    public NewsletterCampaign getById(UUID id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new DomainException("CAMPAIGN_NOT_FOUND",
                        "Campaign not found: " + id));
    }

    /**
     * Get all campaigns for a gym.
     */
    @Transactional(readOnly = true)
    public List<NewsletterCampaign> getByGymId(UUID gymId) {
        return campaignRepository.findByGymId(gymId);
    }

    /**
     * Schedule a campaign for future delivery.
     */
    @Transactional
    public NewsletterCampaign schedule(UUID campaignId, LocalDateTime scheduledAt) {
        NewsletterCampaign campaign = getById(campaignId);
        campaign.schedule(scheduledAt);
        NewsletterCampaign updated = campaignRepository.save(campaign);
        log.info("Scheduled campaign: {} for: {}", campaignId, scheduledAt);
        return updated;
    }

    /**
     * Cancel a scheduled campaign.
     */
    @Transactional
    public NewsletterCampaign cancel(UUID campaignId) {
        NewsletterCampaign campaign = getById(campaignId);
        campaign.cancel();
        NewsletterCampaign updated = campaignRepository.save(campaign);
        log.info("Cancelled campaign: {}", campaignId);
        return updated;
    }

    /**
     * Get audience preview for a campaign.
     */
    @Transactional(readOnly = true)
    public AudiencePreviewResponse getAudiencePreview(UUID campaignId) {
        NewsletterCampaign campaign = getById(campaignId);
        return audienceResolver.getAudiencePreview(
                campaign.getGymId(),
                campaign.getAudienceType(),
                campaign.getAudienceFilter());
    }

    /**
     * Send a campaign immediately.
     */
    @Transactional
    public NewsletterCampaign send(UUID campaignId, UUID sentByUserId) {
        NewsletterCampaign campaign = getById(campaignId);

        if (!campaign.canSend()) {
            throw new DomainException("CAMPAIGN_CANNOT_SEND",
                    "Campaign is not in a valid state to send");
        }

        campaign.startSending();
        campaign.setSentByUserId(sentByUserId);
        campaignRepository.save(campaign);

        // Resolve audience and send asynchronously
        sendCampaignAsync(campaign);

        return campaign;
    }

    /**
     * Asynchronously send emails to all recipients.
     */
    @Async
    public void sendCampaignAsync(NewsletterCampaign campaign) {
        log.info("Starting async send for campaign: {}", campaign.getId());

        List<AudienceResolver.MemberRecipient> recipients = audienceResolver.resolveAudience(
                campaign.getGymId(),
                campaign.getAudienceType(),
                campaign.getAudienceFilter());

        int deliveredCount = 0;
        int failedCount = 0;

        for (AudienceResolver.MemberRecipient recipient : recipients) {
            CampaignRecipient campaignRecipient = CampaignRecipient.builder()
                    .campaignId(campaign.getId())
                    .memberId(recipient.memberId())
                    .email(recipient.email())
                    .build();

            try {
                // Render personalized content
                Map<String, Object> variables = buildRecipientVariables(recipient);
                String subject = templateService.renderSubject(campaign.getSubject(), variables);
                String body = templateService.renderTemplate(campaign.getBody(), variables);

                // Send email
                sendNewsletterEmail(recipient.email(), recipient.firstName(), subject, body);

                campaignRecipient.markSent();
                deliveredCount++;
            } catch (Exception e) {
                log.error("Failed to send to {}: {}", recipient.email(), e.getMessage());
                campaignRecipient.markFailed(e.getMessage());
                failedCount++;
            }

            recipientRepository.save(campaignRecipient);
        }

        // Update campaign stats
        campaign.completeSending(recipients.size(), deliveredCount, failedCount);
        campaignRepository.save(campaign);

        log.info("Completed campaign: {} - Total: {}, Delivered: {}, Failed: {}",
                campaign.getId(), recipients.size(), deliveredCount, failedCount);
    }

    /**
     * Build template variables for a recipient.
     */
    private Map<String, Object> buildRecipientVariables(AudienceResolver.MemberRecipient recipient) {
        Map<String, Object> variables = new HashMap<>();
        String firstName = recipient.firstName() != null ? recipient.firstName() : "";
        String lastName = recipient.lastName() != null ? recipient.lastName() : "";
        variables.put("member_name", (firstName + " " + lastName).trim());
        variables.put("first_name", firstName);
        variables.put("last_name", lastName);
        variables.put("email", recipient.email());
        return variables;
    }

    /**
     * Send a newsletter email using the email service.
     */
    private void sendNewsletterEmail(String to, String firstName, String subject, String htmlBody) {
        emailService.sendHtmlEmail(to, subject, htmlBody);
    }

    /**
     * Delete a campaign (soft delete).
     */
    @Transactional
    public void delete(UUID campaignId) {
        NewsletterCampaign campaign = getById(campaignId);
        if (campaign.getStatus() == CampaignStatus.SENDING) {
            throw new DomainException("CAMPAIGN_IN_PROGRESS",
                    "Cannot delete a campaign that is currently sending");
        }
        campaign.setActive(false);
        campaignRepository.save(campaign);
        log.info("Soft-deleted campaign: {}", campaignId);
    }
}
