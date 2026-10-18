package com.gymmate.notification.internal.application.port;

import tools.jackson.databind.JsonNode;

/** Outbound port: verifies the authenticity of inbound SNS (SES event) messages. */
public interface InboundMessageVerifier {

    boolean verifySignature(JsonNode snsPayload);
}
