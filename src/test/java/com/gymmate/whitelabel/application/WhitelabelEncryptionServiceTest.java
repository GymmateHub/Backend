package com.gymmate.whitelabel.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WhitelabelEncryptionServiceTest {

    private WhitelabelEncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new WhitelabelEncryptionService("TestSecretEncryptionKey123!");
    }

    @Test
    void testEncryptAndDecrypt() {
        String plainText = "MySuperSecretSmtpPassword123!";
        String encrypted = encryptionService.encrypt(plainText);

        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);

        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    void testMaskSecret() {
        String secret = "secretApiKey123";
        String masked = encryptionService.mask(secret);
        assertEquals("••••••••", masked);
    }
}
