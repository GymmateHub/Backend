package com.gymmate.identity.internal.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Validates JWT secret configuration at application startup.
 * SECURITY: Prevents deployment with weak or default JWT secrets.
 */
@Component
@Slf4j
public class JwtSecretValidator {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final int MIN_SECRET_LENGTH = 32; // 256 bits
    private static final String DEFAULT_SECRET = "default-dev-secret-key-change-in-production-minimum-32-chars";

    /**
     * Validates JWT secret when application is ready.
     * Fails fast if secret is insecure.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateJwtSecret() {
        log.info("=================================================================");
        log.info("SECURITY: Validating JWT secret configuration...");
        log.info("=================================================================");

        // Check if default secret is being used
        if (jwtSecret.equals(DEFAULT_SECRET)) {
            String error = """
                    
                    ╔════════════════════════════════════════════════════════════════╗
                    ║  CRITICAL SECURITY ERROR: Default JWT Secret Detected!        ║
                    ╠════════════════════════════════════════════════════════════════╣
                    ║                                                                ║
                    ║  The application is using the DEFAULT JWT secret!             ║
                    ║  This is EXTREMELY DANGEROUS in production environments.      ║
                    ║                                                                ║
                    ║  ACTION REQUIRED:                                              ║
                    ║  Set JWT_SECRET environment variable with a cryptographically ║
                    ║  secure random value.                                          ║
                    ║                                                                ║
                    ║  Generate a secure secret:                                     ║
                    ║    openssl rand -base64 32                                     ║
                    ║                                                                ║
                    ║  Then set it as an environment variable:                       ║
                    ║    export JWT_SECRET="<generated-secret-here>"                 ║
                    ║                                                                ║
                    ╚════════════════════════════════════════════════════════════════╝
                    """;

            log.error(error);
            throw new IllegalStateException(
                    "CRITICAL SECURITY ERROR: Default JWT secret is being used! " +
                    "Set JWT_SECRET environment variable with a cryptographically secure random value.");
        }

        // Check secret length
        byte[] secretBytes;
        try {
            // Try to decode as Base64 first
            secretBytes = Base64.getDecoder().decode(jwtSecret);
            log.debug("JWT secret is Base64 encoded ({} bytes decoded)", secretBytes.length);
        } catch (IllegalArgumentException e) {
            // Not Base64, use as-is
            secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            log.debug("JWT secret is plain text ({} bytes)", secretBytes.length);
        }

        if (secretBytes.length < MIN_SECRET_LENGTH) {
            String error = String.format("""
                    
                    ╔════════════════════════════════════════════════════════════════╗
                    ║  CRITICAL SECURITY ERROR: JWT Secret Too Short!               ║
                    ╠════════════════════════════════════════════════════════════════╣
                    ║                                                                ║
                    ║  Current secret length: %d bytes                              ║
                    ║  Minimum required: %d bytes (256 bits)                        ║
                    ║                                                                ║
                    ║  A short secret is vulnerable to brute-force attacks.         ║
                    ║                                                                ║
                    ║  Generate a secure secret (256 bits):                          ║
                    ║    openssl rand -base64 32                                     ║
                    ║                                                                ║
                    ╚════════════════════════════════════════════════════════════════╝
                    """,
                    secretBytes.length, MIN_SECRET_LENGTH);

            log.error(error);
            throw new IllegalStateException(
                    String.format("CRITICAL SECURITY ERROR: JWT secret is too short (%d bytes). " +
                            "Minimum required: %d bytes (256 bits). " +
                            "Generate a secure secret using: openssl rand -base64 32",
                            secretBytes.length, MIN_SECRET_LENGTH));
        }

        // Check for common weak patterns
        String lowerSecret = jwtSecret.toLowerCase();
        if (lowerSecret.contains("secret") ||
            lowerSecret.contains("password") ||
            lowerSecret.contains("key") ||
            lowerSecret.contains("test") ||
            lowerSecret.contains("demo") ||
            lowerSecret.matches(".*[a-z]{10,}.*")) { // Long sequences of lowercase letters

            log.warn("""
                    
                    ╔════════════════════════════════════════════════════════════════╗
                    ║  WARNING: JWT Secret Appears Weak!                             ║
                    ╠════════════════════════════════════════════════════════════════╣
                    ║                                                                ║
                    ║  The JWT secret contains common words or patterns.             ║
                    ║  This may indicate a non-random secret.                        ║
                    ║                                                                ║
                    ║  RECOMMENDATION:                                               ║
                    ║  Use a cryptographically random secret generated by:           ║
                    ║    openssl rand -base64 32                                     ║
                    ║                                                                ║
                    ╚════════════════════════════════════════════════════════════════╝
                    """);
        }

        log.info("✓ JWT secret validation passed ({} bytes)", secretBytes.length);
        log.info("✓ Secret meets minimum security requirements");
        log.info("=================================================================");
    }
}
