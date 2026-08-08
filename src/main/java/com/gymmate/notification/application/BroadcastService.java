package com.gymmate.notification.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.notification.application.channel.ChannelException;
import com.gymmate.notification.application.channel.ChannelSender;
import com.gymmate.notification.application.port.OrganisationSettingsSource;
import com.gymmate.notification.domain.NotificationChannel;
import com.gymmate.notification.domain.NotificationSettings;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.whitelabel.application.WhitelabelSettingsService;
import com.gymmate.whitelabel.domain.WhitelabelSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for broadcasting notifications via the organisation's preferred channel.
 * Uses tenant's configured credentials.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BroadcastService {

    private final OrganisationSettingsSource organisationSettingsSource;
    private final WhitelabelSettingsService whitelabelSettingsService;
    private final List<ChannelSender> channelSenders;
    private final ObjectMapper objectMapper;

    private Map<NotificationChannel, ChannelSender> senderMap;

    /**
     * Result of a broadcast attempt.
     */
    public record BroadcastResult(
            boolean success,
            NotificationChannel channelUsed,
            boolean fallbackUsed,
            String errorMessage) {
    }

    /**
     * Send a notification to a recipient using the organisation's preferred channel.
     *
     * @param recipient The recipient identifier (email, phone number, etc.)
     * @param email     The recipient's email
     * @param subject   Subject line / title
     * @param body      Message body
     * @return Result indicating success and channel used
     */
    public BroadcastResult send(String recipient, String email, String subject, String body) {
        UUID organisationId = TenantContext.getCurrentTenantId();
        UUID gymId = TenantContext.getCurrentGymId();

        NotificationSettings settings = getNotificationSettings(organisationId);
        NotificationChannel preferredChannel = settings.getPreferredChannel();

        Optional<WhitelabelSettings> whitelabelOpt = whitelabelSettingsService.getWhitelabelSettings(organisationId, gymId);
        if (whitelabelOpt.isPresent()) {
            WhitelabelSettings whitelabel = whitelabelOpt.get();
            if (whitelabel.isWhatsappEnabled()) {
                preferredChannel = NotificationChannel.WHATSAPP;
            } else if (whitelabel.isSmtpEnabled()) {
                preferredChannel = NotificationChannel.EMAIL;
            }
        }

        // Initialize sender map lazily
        if (senderMap == null) {
            senderMap = channelSenders.stream()
                    .collect(Collectors.toMap(ChannelSender::getChannel, Function.identity()));
        }

        // Determine recipient for the channel
        String channelRecipient = getRecipientForChannel(recipient, email, preferredChannel);
        ChannelSender sender = senderMap.get(preferredChannel);

        if (sender != null) {
            try {
                sender.send(channelRecipient, subject, body);
                log.info("Sent notification via {} to {}", preferredChannel, channelRecipient);
                return new BroadcastResult(true, preferredChannel, false, null);
            } catch (ChannelException e) {
                log.error("Failed to send notification via {} to {}: {}", preferredChannel, channelRecipient, e.getMessage());
                return new BroadcastResult(false, preferredChannel, false, e.getMessage());
            }
        }

        return new BroadcastResult(false, null, false, "No channel sender available for " + preferredChannel);
    }

    /**
     * Get notification settings from organisation.
     */
    private NotificationSettings getNotificationSettings(UUID organisationId) {
        if (organisationId == null) {
            log.warn("No organisation context, using default notification settings");
            return new NotificationSettings();
        }

        return organisationSettingsSource.findSettingsJson(organisationId)
                .map(settingsJson -> parseNotificationSettings(organisationId, settingsJson))
                .orElseGet(() -> {
                    log.warn("Organisation not found: {}, using default settings", organisationId);
                    return new NotificationSettings();
                });
    }

    /**
     * Parse NotificationSettings from an organisation's raw settings JSON.
     */
    private NotificationSettings parseNotificationSettings(UUID organisationId, String settingsJson) {
        if (settingsJson == null || settingsJson.isBlank() || "{}".equals(settingsJson)) {
            return new NotificationSettings();
        }

        try {
            return objectMapper.readValue(settingsJson, NotificationSettings.class);
        } catch (Exception e) {
            log.warn("Failed to parse notification settings for org {}: {}", organisationId, e.getMessage());
            return new NotificationSettings();
        }
    }

    /**
     * Get the appropriate recipient identifier for the channel.
     */
    private String getRecipientForChannel(String recipient, String email, NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> email;
            case SMS, WHATSAPP -> recipient; // Assumes recipient is phone number
            case PUSH -> recipient; // Device token or user ID
        };
    }
}
