package com.gymmate.whitelabel.internal.application.dto;

import com.gymmate.whitelabel.internal.domain.WhatsAppProvider;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppTestRequest {

    private WhatsAppProvider whatsappProvider;
    private String whatsappPhoneNumber;

    @NotBlank(message = "WhatsApp Phone Number ID is required")
    private String whatsappPhoneNumberId;

    @NotBlank(message = "WhatsApp API key / Access Token is required")
    private String whatsappApiKey;

    @NotBlank(message = "Recipient phone number is required")
    private String recipientPhoneNumber;
}
