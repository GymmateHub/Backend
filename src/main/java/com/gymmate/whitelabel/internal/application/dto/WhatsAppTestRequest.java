package com.gymmate.whitelabel.internal.application.dto;

import com.gymmate.whitelabel.internal.domain.WhatsAppProvider;
import jakarta.validation.constraints.NotBlank;

public record WhatsAppTestRequest(
        WhatsAppProvider whatsappProvider,
        String whatsappPhoneNumber,
        @NotBlank(message = "WhatsApp Phone Number ID is required")
        String whatsappPhoneNumberId,
        @NotBlank(message = "WhatsApp API key / Access Token is required")
        String whatsappApiKey,
        @NotBlank(message = "Recipient phone number is required")
        String recipientPhoneNumber
) {
}
