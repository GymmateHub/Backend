package com.gymmate.notification.application;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Verifies the cryptographic signature of incoming Amazon SNS webhook notifications.
 * Validates signing certificate URLs against the official AWS SNS hostname pattern,
 * caches retrieved X.509 certificates, and checks RSA signatures.
 */
@Component
@Slf4j
public class SnsSignatureVerifier {

    private static final Pattern CERT_URL_PATTERN = Pattern.compile("^https://sns\\.[a-z0-9\\-]+\\.amazonaws\\.com/.*\\.pem$");
    private final Map<String, PublicKey> certificateCache = new ConcurrentHashMap<>();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Verify whether the SNS message signature is valid.
     */
    public boolean verifySignature(JsonNode snsPayload) {
        try {
            String certUrl = snsPayload.path("SigningCertURL").asText(null);
            if (certUrl == null || !CERT_URL_PATTERN.matcher(certUrl).matches()) {
                log.error("Rejected invalid SNS SigningCertURL: {}", certUrl);
                return false;
            }

            String signature = snsPayload.path("Signature").asText(null);
            if (signature == null || signature.isBlank()) {
                log.error("Missing Signature in SNS payload");
                return false;
            }

            String signatureVersion = snsPayload.path("SignatureVersion").asText("1");
            String type = snsPayload.path("Type").asText("");

            String canonicalString = buildCanonicalString(snsPayload, type);
            if (canonicalString == null) {
                log.error("Unsupported SNS message type for signature verification: {}", type);
                return false;
            }

            PublicKey publicKey = getPublicKey(certUrl);
            if (publicKey == null) {
                return false;
            }

            String algorithm = "2".equals(signatureVersion) ? "SHA256withRSA" : "SHA1withRSA";
            Signature sig = Signature.getInstance(algorithm);
            sig.initVerify(publicKey);
            sig.update(canonicalString.getBytes(StandardCharsets.UTF_8));

            byte[] decodedSignature = Base64.getDecoder().decode(signature);
            boolean verified = sig.verify(decodedSignature);
            if (!verified) {
                log.warn("SNS signature verification failed for message: {}", snsPayload.path("MessageId").asText());
            }
            return verified;
        } catch (Exception e) {
            log.error("Error verifying SNS signature: {}", e.getMessage(), e);
            return false;
        }
    }

    private PublicKey getPublicKey(String certUrl) {
        return certificateCache.computeIfAbsent(certUrl, url -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build();

                HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
                if (response.statusCode() != 200) {
                    log.error("Failed to fetch SNS signing cert from {}: status {}", url, response.statusCode());
                    return null;
                }

                CertificateFactory fact = CertificateFactory.getInstance("X.509");
                try (InputStream in = response.body()) {
                    X509Certificate cert = (X509Certificate) fact.generateCertificate(in);
                    return cert.getPublicKey();
                }
            } catch (Exception e) {
                log.error("Failed to load or parse SNS certificate from {}: {}", url, e.getMessage(), e);
                return null;
            }
        });
    }

    private String buildCanonicalString(JsonNode payload, String type) {
        StringBuilder sb = new StringBuilder();

        if ("Notification".equals(type)) {
            appendField(sb, "Message", payload.path("Message").asText(null));
            appendField(sb, "MessageId", payload.path("MessageId").asText(null));
            if (payload.hasNonNull("Subject")) {
                appendField(sb, "Subject", payload.path("Subject").asText());
            }
            appendField(sb, "Timestamp", payload.path("Timestamp").asText(null));
            appendField(sb, "TopicArn", payload.path("TopicArn").asText(null));
            appendField(sb, "Type", type);
            return sb.toString();
        } else if ("SubscriptionConfirmation".equals(type) || "UnsubscribeConfirmation".equals(type)) {
            appendField(sb, "Message", payload.path("Message").asText(null));
            appendField(sb, "MessageId", payload.path("MessageId").asText(null));
            appendField(sb, "SubscribeURL", payload.path("SubscribeURL").asText(null));
            appendField(sb, "Timestamp", payload.path("Timestamp").asText(null));
            appendField(sb, "Token", payload.path("Token").asText(null));
            appendField(sb, "TopicArn", payload.path("TopicArn").asText(null));
            appendField(sb, "Type", type);
            return sb.toString();
        }

        return null;
    }

    private void appendField(StringBuilder sb, String key, String value) {
        if (value != null) {
            sb.append(key).append("\n").append(value).append("\n");
        }
    }
}
