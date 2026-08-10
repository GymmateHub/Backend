package com.gymmate.whitelabel.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Service for encrypting and decrypting sensitive credentials (SMTP passwords,
 * WhatsApp API keys, Newsletter API keys) at rest.
 */
@Service
@Slf4j
public class WhitelabelEncryptionService {

    private static final String ALGORITHM = "AES";
    private final SecretKeySpec secretKeySpec;

    public WhitelabelEncryptionService(
            @Value("${app.whitelabel.encryption-key:GymMateWhitelabelEncryptionKey2026!}") String secretKey) {
        try {
            byte[] key = secretKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // 128-bit AES key
            this.secretKeySpec = new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            log.error("Failed to initialize WhitelabelEncryptionService secret key", e);
            throw new RuntimeException("Failed to initialize Whitelabel Encryption Service", e);
        }
    }

    /**
     * Encrypt a plaintext secret.
     */
    public String encrypt(String plainText) {
        if (!StringUtils.hasText(plainText)) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Error encrypting secret data", e);
            throw new RuntimeException("Error encrypting secret data", e);
        }
    }

    /**
     * Decrypt an encrypted secret.
     */
    public String decrypt(String encryptedText) {
        if (!StringUtils.hasText(encryptedText)) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error decrypting secret data", e);
            throw new RuntimeException("Error decrypting secret data", e);
        }
    }

    /**
     * Mask sensitive strings for REST API responses (e.g. "••••••••").
     */
    public String mask(String secret) {
        if (!StringUtils.hasText(secret)) {
            return null;
        }
        return "••••••••";
    }
}
