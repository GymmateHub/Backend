package com.gymmate.shared.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

/**
 * Utility class to generate a secure JWT secret key.
 * Run this class to generate a Base64-encoded secret key for your application.
 *
 * Usage:
 * 1. Run this main method
 * 2. Copy the generated Base64 secret
 * 3. Set it as JWT_SECRET in your environment variables or .env file
 *
 * The secret will be at least 256 bits (32 bytes) as required for HS256 algorithm.
 */
public class JwtSecretGenerator {

    public static void main(String[] args) {
        // Generate a secure key for HS512 (512 bits / 64 bytes)
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

        // Encode to Base64
        String base64Secret = Encoders.BASE64.encode(key.getEncoded());

        System.out.println("=".repeat(80));
        System.out.println("JWT SECRET KEY GENERATOR");
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("Your secure JWT secret key (Base64 encoded):");
        System.out.println();
        System.out.println(base64Secret);
        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println("Add this to your .env file as:");
        System.out.println();
        System.out.println("JWT_SECRET=" + base64Secret);
        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println("Key length: " + key.getEncoded().length + " bytes (" + (key.getEncoded().length * 8) + " bits)");
        System.out.println("Algorithm: " + key.getAlgorithm());
        System.out.println("=".repeat(80));
    }
}

