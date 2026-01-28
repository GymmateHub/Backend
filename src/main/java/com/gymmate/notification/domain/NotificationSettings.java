package com.gymmate.notification.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Settings for notification preferences, stored in Organisation.settings JSON.
 * Example JSON: {"preferredChannel": "EMAIL", "smsEnabled": false,
 * "whatsappEnabled": false}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationSettings {

    @Builder.Default
    private NotificationChannel preferredChannel = NotificationChannel.EMAIL;

    @Builder.Default
    private boolean smsEnabled = false;

    @Builder.Default
    private boolean whatsappEnabled = false;

    @Builder.Default
    private boolean pushEnabled = false;

    // SMS Configuration (for Twilio)
    private String twilioAccountSid;
    private String twilioAuthToken;
    private String twilioFromNumber;

    // WhatsApp Configuration
    private String whatsappBusinessId;

    /**
     * Check if the specified channel is available for use.
     */
    public boolean isChannelEnabled(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> true; // Email is always enabled
            case SMS -> smsEnabled && twilioFromNumber != null;
            case WHATSAPP -> whatsappEnabled && whatsappBusinessId != null;
            case PUSH -> pushEnabled;
        };
    }

    /**
     * Get fallback channel (always EMAIL).
     */
    public NotificationChannel getFallbackChannel() {
        return NotificationChannel.EMAIL;
    }
}
