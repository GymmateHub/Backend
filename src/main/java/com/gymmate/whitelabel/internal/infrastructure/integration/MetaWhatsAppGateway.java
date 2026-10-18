package com.gymmate.whitelabel.internal.infrastructure.integration;

import com.gymmate.shared.exception.DomainException;
import com.gymmate.whitelabel.internal.application.port.WhatsAppGateway;
import com.gymmate.whitelabel.internal.domain.WhatsAppProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/** WhatsApp delivery through the Meta Cloud API (Graph API v18.0). */
@Slf4j
@Component
class MetaWhatsAppGateway implements WhatsAppGateway {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void send(WhatsAppProvider provider, String phoneNumberId, String apiKey, String recipientPhoneNumber,
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
}
