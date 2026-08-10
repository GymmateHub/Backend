package com.gymmate.whitelabel.application;

import com.gymmate.shared.exception.DomainException;
import com.gymmate.whitelabel.api.dto.WhitelabelSettingsRequest;
import com.gymmate.whitelabel.api.dto.WhitelabelSettingsResponse;
import com.gymmate.whitelabel.domain.WhatsAppProvider;
import com.gymmate.whitelabel.domain.WhitelabelSettings;
import com.gymmate.whitelabel.infrastructure.WhitelabelSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

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
    private final DynamicMailSenderFactory mailSenderFactory;
    private final RestTemplate restTemplate = new RestTemplate();

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
            return new WhitelabelSettingsResponse();
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

        try {
            String cleanPhone = recipientPhoneNumber.replaceAll("[^0-9]", "");
            String url = "https://graph.facebook.com/v18.0/" + phoneNumberId + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> textObj = new HashMap<>();
            textObj.put("body", messageText);

            Map<String, Object> payload = new HashMap<>();
            payload.put("messaging_product", "whatsapp");
            payload.put("to", cleanPhone);
            payload.put("type", "text");
            payload.put("text", textObj);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully sent WhatsApp message to {}", cleanPhone);
            } else {
                log.error("WhatsApp API call failed with status: {}, body: {}", response.getStatusCode(), response.getBody());
                throw new DomainException("WHATSAPP_SEND_FAILED", "WhatsApp API returned status " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}", recipientPhoneNumber, e);
            throw new DomainException("WHATSAPP_SEND_FAILED", "WhatsApp sending failed: " + e.getMessage());
        }
    }

    private void updateSettingsFromRequest(WhitelabelSettings settings, WhitelabelSettingsRequest request) {
        // Branding
        if (request.getBrandName() != null) settings.setBrandName(request.getBrandName());
        if (request.getLogoUrl() != null) settings.setLogoUrl(request.getLogoUrl());
        if (request.getFaviconUrl() != null) settings.setFaviconUrl(request.getFaviconUrl());
        if (request.getPrimaryColor() != null) settings.setPrimaryColor(request.getPrimaryColor());
        if (request.getSecondaryColor() != null) settings.setSecondaryColor(request.getSecondaryColor());
        if (request.getCustomDomain() != null) settings.setCustomDomain(request.getCustomDomain());
        if (request.getEmailHeaderLogoUrl() != null) settings.setEmailHeaderLogoUrl(request.getEmailHeaderLogoUrl());
        if (request.getEmailFooterText() != null) settings.setEmailFooterText(request.getEmailFooterText());
        if (request.getSupportEmail() != null) settings.setSupportEmail(request.getSupportEmail());
        if (request.getSupportPhone() != null) settings.setSupportPhone(request.getSupportPhone());

        // SMTP Configuration
        settings.setSmtpEnabled(request.isSmtpEnabled());
        if (request.getSmtpHost() != null) settings.setSmtpHost(request.getSmtpHost());
        if (request.getSmtpPort() != null) settings.setSmtpPort(request.getSmtpPort());
        if (request.getSmtpUsername() != null) settings.setSmtpUsername(request.getSmtpUsername());
        if (StringUtils.hasText(request.getSmtpPassword()) && !"••••••••".equals(request.getSmtpPassword())) {
            settings.setSmtpPasswordEncrypted(encryptionService.encrypt(request.getSmtpPassword()));
        }
        if (request.getSmtpSecurity() != null) settings.setSmtpSecurity(request.getSmtpSecurity());
        if (request.getSmtpFromEmail() != null) settings.setSmtpFromEmail(request.getSmtpFromEmail());
        if (request.getSmtpFromName() != null) settings.setSmtpFromName(request.getSmtpFromName());

        // WhatsApp Configuration
        settings.setWhatsappEnabled(request.isWhatsappEnabled());
        if (request.getWhatsappProvider() != null) settings.setWhatsappProvider(request.getWhatsappProvider());
        if (request.getWhatsappPhoneNumber() != null) settings.setWhatsappPhoneNumber(request.getWhatsappPhoneNumber());
        if (request.getWhatsappPhoneNumberId() != null) settings.setWhatsappPhoneNumberId(request.getWhatsappPhoneNumberId());
        if (request.getWhatsappBusinessId() != null) settings.setWhatsappBusinessId(request.getWhatsappBusinessId());
        if (StringUtils.hasText(request.getWhatsappApiKey()) && !"••••••••".equals(request.getWhatsappApiKey())) {
            settings.setWhatsappApiKeyEncrypted(encryptionService.encrypt(request.getWhatsappApiKey()));
        }

        // Newsletter Configuration
        settings.setNewsletterEnabled(request.isNewsletterEnabled());
        if (request.getNewsletterProvider() != null) settings.setNewsletterProvider(request.getNewsletterProvider());
        if (StringUtils.hasText(request.getNewsletterApiKey()) && !"••••••••".equals(request.getNewsletterApiKey())) {
            settings.setNewsletterApiKeyEncrypted(encryptionService.encrypt(request.getNewsletterApiKey()));
        }
        if (request.getNewsletterListId() != null) settings.setNewsletterListId(request.getNewsletterListId());
        if (request.getNewsletterSenderEmail() != null) settings.setNewsletterSenderEmail(request.getNewsletterSenderEmail());
        if (request.getNewsletterSenderName() != null) settings.setNewsletterSenderName(request.getNewsletterSenderName());
    }

    private WhitelabelSettingsResponse toResponse(WhitelabelSettings settings) {
        boolean isSmtpPasswordSet = StringUtils.hasText(settings.getSmtpPasswordEncrypted());
        boolean isWhatsAppApiKeySet = StringUtils.hasText(settings.getWhatsappApiKeyEncrypted());
        boolean isNewsletterApiKeySet = StringUtils.hasText(settings.getNewsletterApiKeyEncrypted());

        return WhitelabelSettingsResponse.fromEntity(settings, isSmtpPasswordSet, isWhatsAppApiKeySet, isNewsletterApiKeySet);
    }
}
