package com.gymmate.shared.security.service;

import com.gymmate.shared.security.repository.TokenBlacklistRepository;
import com.gymmate.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWT Service for token generation, validation, and claim extraction.
 * Handles both access tokens (short-lived) and refresh tokens (long-lived).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:900000}") // 15 minutes default
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7 days default
    private long refreshExpiration;

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";

    // ==================== TOKEN GENERATION ====================

    /**
     * Generate JWT access token from User entity with optional gym context.
     */
    public String generateToken(User user, UUID currentGymId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);
        claims.put("userId", user.getId().toString());
        claims.put("email", user.getEmail());
        claims.put("organisationId", uuidToString(user.getOrganisationId()));
        claims.put("tenantId", uuidToString(user.getOrganisationId()));
        claims.put("gymId", uuidToString(currentGymId));
        claims.put("role", user.getRole().name());
        claims.put("emailVerified", user.isEmailVerified());

        return buildToken(claims, user.getEmail(), jwtExpiration);
    }

    /**
     * Generate JWT access token from User entity without gym context.
     */
    public String generateToken(User user) {
        return generateToken(user, null);
    }

    /**
     * Generate refresh token from User entity.
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH);
        claims.put("userId", user.getId().toString());
        claims.put("organisationId", uuidToString(user.getOrganisationId()));
        claims.put("tenantId", uuidToString(user.getOrganisationId()));

        return buildToken(claims, user.getEmail(), refreshExpiration);
    }

    /**
     * Generate short-lived verification token for email verification.
     */
    public String generateVerificationToken(String registrationId, String email, int expiryMinutes) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("registrationId", registrationId);
        claims.put("type", "verification");

        return buildToken(claims, email, expiryMinutes * 60_000L);
    }

    // ==================== TOKEN VALIDATION ====================

    /**
     * Validate token structure, expiration, and blacklist status.
     */
    public boolean validateToken(String token) {
        try {
            if (isTokenBlacklisted(token)) {
                log.warn("Token is blacklisted");
                return false;
            }
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate verification token and extract claims.
     */
    public Claims validateVerificationToken(String token) {
        Claims claims = extractAllClaims(token);

        String type = claims.get("type", String.class);
        if (!"verification".equals(type)) {
            throw new IllegalArgumentException("Invalid token type");
        }

        if (isTokenExpired(token)) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        return claims;
    }

    /**
     * Check if a token is blacklisted.
     */
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }

    // ==================== CLAIM EXTRACTION ====================

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractUserId(String token) {
        String userId = extractClaim(token, claims -> claims.get("userId", String.class));
        if (userId == null) {
            userId = extractClaim(token, Claims::getSubject);
        }
        return userId != null ? UUID.fromString(userId) : null;
    }

    public UUID extractGymId(String token) {
        String gymId = extractClaim(token, claims -> claims.get("gymId", String.class));
        if (gymId == null) {
            gymId = extractClaim(token, claims -> claims.get("tenantId", String.class));
        }
        return parseUuid(gymId);
    }

    public UUID extractOrganisationId(String token) {
        String orgId = extractClaim(token, claims -> claims.get("organisationId", String.class));
        if (orgId == null) {
            orgId = extractClaim(token, claims -> claims.get("tenantId", String.class));
        }
        return parseUuid(orgId);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    // ==================== PRIVATE HELPERS ====================

    private String buildToken(Map<String, Object> claims, String subject, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private SecretKey getSigningKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            if (keyBytes.length < 32) {
                log.warn("JWT secret is too short. Minimum 256 bits (32 bytes) required. Current: {} bytes",
                        keyBytes.length);
            }
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.warn("JWT secret is not Base64 encoded. Using UTF-8 bytes as fallback.");
            byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 32) {
                throw new IllegalArgumentException("JWT secret is too short. Must be at least 256 bits (32 bytes).");
            }
            return Keys.hmacShaKeyFor(keyBytes);
        }
    }

    private String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    private UUID parseUuid(String value) {
        return value != null && !"null".equals(value) ? UUID.fromString(value) : null;
    }
}
