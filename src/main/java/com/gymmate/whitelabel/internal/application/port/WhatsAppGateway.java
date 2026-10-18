package com.gymmate.whitelabel.internal.application.port;

import com.gymmate.whitelabel.internal.domain.WhatsAppProvider;

/** Outbound port: delivers a WhatsApp text message through the tenant's provider. */
public interface WhatsAppGateway {

    /**
     * @throws com.gymmate.shared.exception.DomainException WHATSAPP_SEND_FAILED when delivery fails
     */
    void send(WhatsAppProvider provider, String phoneNumberId, String apiKey, String recipientPhoneNumber,
              String messageText);
}
