package com.gymmate.notification.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.notification.application.channel.ChannelException;
import com.gymmate.notification.application.channel.ChannelSender;
import com.gymmate.notification.domain.NotificationChannel;
import com.gymmate.notification.domain.NotificationSettings;
import com.gymmate.organisation.domain.Organisation;
import com.gymmate.organisation.infrastructure.OrganisationRepository;
import com.gymmate.shared.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for broadcasting notifications via the organisation's preferred
 * channel.
 * Falls back to email if the preferred channel fails or is not configured.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BroadcastService {

    private final OrganisationRepository organisationRepository;
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
     * Send a notification to a recipient using the organisation's preferred
     * channel.
     * Falls back to email if the preferred channel fails.
     *
     * @param recipient The recipient identifier (email, phone number, etc.)
     * @param email     The recipient's email for fallback
     * @param subject   Subject line / title
     * @param body      Message body (HTML for email, plain text for SMS)
     * @return Result indicating success, channel used, and whether fallback was
     *         needed
     */
    public BroadcastResult send(String recipient, String email, String subject, String body) {
        UUID organisationId = TenantContext.getCurrentTenantId();
        NotificationSettings settings = getNotificationSettings(organisationId);
        NotificationChannel preferredChannel = settings.getPreferredChannel();

        // Initialize sender map lazily
        if (senderMap == null) {
            senderMap = channelSenders.stream()
                    .collect(Collectors.toMap(ChannelSender::getChannel, Function.identity()));
        }

        // Determine recipient for the channel
        String channelRecipient = getRecipientForChannel(recipient, email, preferredChannel);

        // Try preferred channel first (if not EMAIL and if configured)
        if (preferredChannel != NotificationChannel.EMAIL && settings.isChannelEnabled(preferredChannel)) {
            ChannelSender preferredSender = senderMap.get(preferredChannel);
            if (preferredSender != null) {
                try {
                    preferredSender.send(channelRecipient, subject, body);
                    log.info("Sent notification via {} to {}", preferredChannel, channelRecipient);
                    return new BroadcastResult(true, preferredChannel, false, null);
                } catch (ChannelException e) {
                    log.warn("Preferred channel {} failed for {}: {}, falling back to email",
                            preferredChannel, channelRecipient, e.getMessage());
                }
            }
        }

        // Fallback to email (or primary if EMAIL is preferred)
        ChannelSender emailSender = senderMap.get(NotificationChannel.EMAIL);
        if (emailSender != null) {
            try {
                emailSender.send(email, subject, body);
                boolean wasFallback = preferredChannel != NotificationChannel.EMAIL;
                log.info("Sent notification via EMAIL to {} (fallback: {})", email, wasFallback);
                return new BroadcastResult(true, NotificationChannel.EMAIL, wasFallback, null);
            } catch (ChannelException e) {
                log.error("Email fallback also failed for {}: {}", email, e.getMessage());
                return new BroadcastResult(false, NotificationChannel.EMAIL, true, e.getMessage());
            }
        }

        return new BroadcastResult(false, null, false, "No channel senders available");
    }

    /**
     * Get notification settings from organisation.
     */
    private NotificationSettings getNotificationSettings(UUID organisationId) {
        if (organisationId == null) {
            log.warn("No organisation context, using default notification settings");
            return new NotificationSettings();
        }

        return organisationRepository.findById(organisationId)
                .map(this::parseNotificationSettings)
                .orElseGet(() -> {
                    log.warn("Organisation not found: {}, using default settings", organisationId);
                    return new NotificationSettings();
                });
    }

    /**
     * Parse NotificationSettings from Organisation.settings JSON.
     */
    private NotificationSettings parseNotificationSettings(Organisation organisation) {
        String settingsJson = organisation.getSettings();
        if (settingsJson == null || settingsJson.isBlank() || "{}".equals(settingsJson)) {
            return new NotificationSettings();
        }

        try {
            return objectMapper.readValue(settingsJson, NotificationSettings.class);
        } catch (Exception e) {
            log.warn("Failed to parse notification settings for org {}: {}",
                    organisation.getId(), e.getMessage());
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
