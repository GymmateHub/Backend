package com.gymmate.whitelabel.internal.application;

import com.gymmate.whitelabel.internal.application.port.TenantMailSenders;
import com.gymmate.whitelabel.internal.application.port.WhatsAppGateway;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.whitelabel.internal.application.dto.WhitelabelSettingsRequest;
import com.gymmate.whitelabel.internal.application.dto.WhitelabelSettingsResponse;
import com.gymmate.whitelabel.internal.domain.WhatsAppProvider;
import com.gymmate.whitelabel.internal.domain.WhitelabelSettings;
import com.gymmate.whitelabel.internal.application.port.WhitelabelSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WhitelabelSettingsService {

    private final WhitelabelSettingsRepository settingsRepository;
    private final WhitelabelEncryptionService encryptionService;
    private final TenantMailSenders mailSenderFactory;
    private final WhatsAppGateway whatsAppGateway;

    /**
     * Get active whitelabel settings for tenant.
     * Checks Gym-level settings first; if not present, falls back to Organisation-level.
     */
    @Transactional(readOnly = true)
    public Optional<WhitelabelSettings> getWhitelabelSettings(UUID organisationId, UUID gymId) {
        if (gymId != null) {
            Optional<WhitelabelSettings> gymSettings = settingsRepository.findByOrganisationIdAndGymId(organisationId, gymId);
            if (gymSettings.isPresent()) {
                return gymSettings;
            }
        }
        if (organisationId != null) {
            return settingsRepository.findByOrganisationIdAndGymIdIsNull(organisationId);
        }
        return Optional.empty();
    }

    /**
     * Get organisation whitelabel response.
     */
    @Transactional(readOnly = true)
    public WhitelabelSettingsResponse getOrganisationSettingsResponse(UUID organisationId) {
        Optional<WhitelabelSettings> settingsOpt = settingsRepository.findByOrganisationIdAndGymIdIsNull(organisationId);
        if (settingsOpt.isEmpty()) {
            return WhitelabelSettingsResponse.empty();
        }
        WhitelabelSettings settings = settingsOpt.get();
        return toResponse(settings);
    }

    /**
     * Get gym whitelabel response.
     */
    @Transactional(readOnly = true)
    public WhitelabelSettingsResponse getGymSettingsResponse(UUID organisationId, UUID gymId) {
        Optional<WhitelabelSettings> settingsOpt = settingsRepository.findByOrganisationIdAndGymId(organisationId, gymId);
        if (settingsOpt.isEmpty()) {
            return getOrganisationSettingsResponse(organisationId);
        }
        return toResponse(settingsOpt.get());
    }

    /**
     * Save/Update Organisation whitelabel settings.
     */
    @Transactional
    public WhitelabelSettingsResponse saveOrganisationSettings(UUID organisationId, WhitelabelSettingsRequest request) {
        WhitelabelSettings settings = settingsRepository.findByOrganisationIdAndGymIdIsNull(organisationId)
                .orElseGet(() -> {
                    WhitelabelSettings newSettings = WhitelabelSettings.builder().build();
                    newSettings.setOrganisationId(organisationId);
                    newSettings.setGymId(null);
                    return newSettings;
                });

        updateSettingsFromRequest(settings, request);

        WhitelabelSettings saved = settingsRepository.save(settings);
        mailSenderFactory.evictCache(organisationId, null);
        log.info("Updated Organisation Whitelabel settings for org: {}", organisationId);
        return toResponse(saved);
    }

    /**
     * Save/Update Gym whitelabel settings.
     */
    @Transactional
    public WhitelabelSettingsResponse saveGymSettings(UUID organisationId, UUID gymId, WhitelabelSettingsRequest request) {
        if (gymId == null) {
            throw new DomainException("INVALID_GYM_ID", "Gym ID is required for gym-level settings");
        }

        WhitelabelSettings settings = settingsRepository.findByOrganisationIdAndGymId(organisationId, gymId)
                .orElseGet(() -> {
                    WhitelabelSettings newSettings = WhitelabelSettings.builder().build();
                    newSettings.setOrganisationId(organisationId);
                    newSettings.setGymId(gymId);
                    return newSettings;
                });

        updateSettingsFromRequest(settings, request);

        WhitelabelSettings saved = settingsRepository.save(settings);
        mailSenderFactory.evictCache(organisationId, gymId);
        log.info("Updated Gym Whitelabel settings for gym: {}", gymId);
        return toResponse(saved);
    }

    /**
     * Send WhatsApp text message via Meta WhatsApp Cloud API.
     */
    public void sendWhatsAppMessage(
            WhatsAppProvider provider,
            String phoneNumberId,
            String apiKey,
            String recipientPhoneNumber,
            String messageText) {
        whatsAppGateway.send(provider, phoneNumberId, apiKey, recipientPhoneNumber, messageText);
    }

    private void updateSettingsFromRequest(WhitelabelSettings settings, WhitelabelSettingsRequest request) {
        // Branding
        if (request.brandName() != null) settings.setBrandName(request.brandName());
        if (request.logoUrl() != null) settings.setLogoUrl(request.logoUrl());
        if (request.faviconUrl() != null) settings.setFaviconUrl(request.faviconUrl());
        if (request.primaryColor() != null) settings.setPrimaryColor(request.primaryColor());
        if (request.secondaryColor() != null) settings.setSecondaryColor(request.secondaryColor());
        if (request.customDomain() != null) settings.setCustomDomain(request.customDomain());
        if (request.emailHeaderLogoUrl() != null) settings.setEmailHeaderLogoUrl(request.emailHeaderLogoUrl());
        if (request.emailFooterText() != null) settings.setEmailFooterText(request.emailFooterText());
        if (request.supportEmail() != null) settings.setSupportEmail(request.supportEmail());
        if (request.supportPhone() != null) settings.setSupportPhone(request.supportPhone());

        // SMTP Configuration
        settings.setSmtpEnabled(request.smtpEnabled());
        if (request.smtpHost() != null) settings.setSmtpHost(request.smtpHost());
        if (request.smtpPort() != null) settings.setSmtpPort(request.smtpPort());
        if (request.smtpUsername() != null) settings.setSmtpUsername(request.smtpUsername());
        if (StringUtils.hasText(request.smtpPassword()) && !"••••••••".equals(request.smtpPassword())) {
            settings.setSmtpPasswordEncrypted(encryptionService.encrypt(request.smtpPassword()));
        }
        if (request.smtpSecurity() != null) settings.setSmtpSecurity(request.smtpSecurity());
        if (request.smtpFromEmail() != null) settings.setSmtpFromEmail(request.smtpFromEmail());
        if (request.smtpFromName() != null) settings.setSmtpFromName(request.smtpFromName());

        // WhatsApp Configuration
        settings.setWhatsappEnabled(request.whatsappEnabled());
        if (request.whatsappProvider() != null) settings.setWhatsappProvider(request.whatsappProvider());
        if (request.whatsappPhoneNumber() != null) settings.setWhatsappPhoneNumber(request.whatsappPhoneNumber());
        if (request.whatsappPhoneNumberId() != null) settings.setWhatsappPhoneNumberId(request.whatsappPhoneNumberId());
        if (request.whatsappBusinessId() != null) settings.setWhatsappBusinessId(request.whatsappBusinessId());
        if (StringUtils.hasText(request.whatsappApiKey()) && !"••••••••".equals(request.whatsappApiKey())) {
            settings.setWhatsappApiKeyEncrypted(encryptionService.encrypt(request.whatsappApiKey()));
        }

        // Newsletter Configuration
        settings.setNewsletterEnabled(request.newsletterEnabled());
        if (request.newsletterProvider() != null) settings.setNewsletterProvider(request.newsletterProvider());
        if (StringUtils.hasText(request.newsletterApiKey()) && !"••••••••".equals(request.newsletterApiKey())) {
            settings.setNewsletterApiKeyEncrypted(encryptionService.encrypt(request.newsletterApiKey()));
        }
        if (request.newsletterListId() != null) settings.setNewsletterListId(request.newsletterListId());
        if (request.newsletterSenderEmail() != null) settings.setNewsletterSenderEmail(request.newsletterSenderEmail());
        if (request.newsletterSenderName() != null) settings.setNewsletterSenderName(request.newsletterSenderName());
    }

    private WhitelabelSettingsResponse toResponse(WhitelabelSettings settings) {
        boolean isSmtpPasswordSet = StringUtils.hasText(settings.getSmtpPasswordEncrypted());
        boolean isWhatsAppApiKeySet = StringUtils.hasText(settings.getWhatsappApiKeyEncrypted());
        boolean isNewsletterApiKeySet = StringUtils.hasText(settings.getNewsletterApiKeyEncrypted());

        return WhitelabelSettingsResponse.fromEntity(settings, isSmtpPasswordSet, isWhatsAppApiKeySet, isNewsletterApiKeySet);
    }
}
