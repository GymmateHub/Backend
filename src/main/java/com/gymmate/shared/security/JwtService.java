package com.gymmate.shared.security;

import com.gymmate.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

  private final TokenBlacklistRepository tokenBlacklistRepository;

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.expiration:86400000}") // 24 hours default
  private long jwtExpiration;

  @Value("${jwt.refresh-expiration:604800000}") // 7 days default
  private long refreshExpiration;

  /**
   * Generate JWT token with user details and gym context
   */
  public String generateToken(TenantAwareUserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userDetails.getUserId().toString());
    claims.put("email", userDetails.getUsername());
    claims.put("tenantId", userDetails.getGymId().toString());
    claims.put("gymId", userDetails.getGymId().toString());
    claims.put("role", userDetails.getRole());
    claims.put("emailVerified", userDetails.isEmailVerified());

    return createToken(claims, userDetails.getUsername(), jwtExpiration);
  }

  /**
   * Generate JWT token directly from User entity with specific tenant/gym context
   */
  public String generateToken(User user, UUID gymId) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId().toString());
    claims.put("email", user.getEmail());
    claims.put("gymId", gymId != null ? gymId.toString() : null);
    claims.put("role", user.getRole().name());
    claims.put("emailVerified", user.isEmailVerified());
    claims.put("tenantId", gymId != null ? gymId.toString() : null);

    return createToken(claims, user.getEmail(), jwtExpiration);
  }

  /**
   * Generate JWT token directly from User entity (uses user's associated gymId)
   */
  public String generateToken(User user) {
    return generateToken(user, user.getGymId());
  }

  /**
   * Generate refresh token directly from User entity
   */
  public String generateRefreshToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId().toString());
    claims.put("gymId", user.getGymId() != null ? user.getGymId().toString() : null);
    claims.put("email", user.getEmail());
    claims.put("role", user.getRole().name());
    claims.put("emailVerified", user.isEmailVerified());

    return createToken(claims, user.getEmail(), refreshExpiration);
  }

  /**
   * Generate refresh token
   */
  public String generateRefreshToken(TenantAwareUserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userDetails.getUserId().toString());
    claims.put("gymId", userDetails.getGymId().toString());
    claims.put("email", userDetails.getEmail());
    claims.put("role", userDetails.getRole());
    claims.put("emailVerified", userDetails.isEmailVerified());

    return createToken(claims, userDetails.getUsername(), refreshExpiration);
  }

  /**
   * Create token with claims and expiration
   */
  private String createToken(Map<String, Object> claims, String subject, long expiration) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
      .claims(claims)
      .subject(subject)
      .issuedAt(now)
      .expiration(expiryDate)
      .signWith(getSigningKey())
      .compact();
  }

  /**
   * Extract username (email) from token
   */
  public String extractUserName(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extract user ID from token
   */
  public UUID extractUserId(String token) {
    String userId = extractClaim(token, claims -> claims.get("userId", String.class));
    if (userId == null) {
      // Fallback to subject if userId claim doesn't exist (for backward compatibility)
      userId = extractClaim(token, Claims::getSubject);
    }
    return userId != null ? UUID.fromString(userId) : null;
  }

  /**
   * Extract gym ID (tenant ID) from token
   */
  public UUID extractGymId(String token) {
    String gymId = extractClaim(token, claims -> claims.get("gymId", String.class));
    if (gymId == null) {
      // Fallback to tenantId if gymId claim doesn't exist (for backward compatibility)
      gymId = extractClaim(token, claims -> claims.get("tenantId", String.class));
    }
    return gymId != null ? UUID.fromString(gymId) : null;
  }

  /**
   * Extract role from token
   */
  public String extractRole(String token) {
    return extractClaim(token, claims -> claims.get("role", String.class));
  }

  /**
   * Extract expiration date from token
   */
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Extract specific claim from token
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Extract all claims from token
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
      .setSigningKey(getSigningKey())
      .build()
      .parseClaimsJws(token)
      .getBody();
  }

  public Boolean isTokenValid(String token, TenantAwareUserDetails userDetails) {
    final String username = extractUserName(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  /**
   * Check if token is expired
   */
  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Validate token against user details
   */
  public Boolean validateToken(String token, TenantAwareUserDetails userDetails) {
    // Check if token is blacklisted first
    if (isTokenBlacklisted(token)) {
      log.warn("Token is blacklisted");
      return false;
    }

    final String username = extractUserName(token);
    final UUID gymId = extractGymId(token);

    return (username.equals(userDetails.getUsername()) &&
      gymId.equals(userDetails.getGymId()) &&
      !isTokenExpired(token));
  }

  /**
   * Validate token structure and expiration (without user details)
   */
  public Boolean validateToken(String token) {
    try {
      // Check if token is blacklisted first
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
   * Check if a token is blacklisted
   */
  public boolean isTokenBlacklisted(String token) {
    return tokenBlacklistRepository.existsByToken(token);
  }

  /**
   * Get signing key from secret
   * Properly decodes Base64 encoded secret for secure key generation
   */
  private Key getSigningKey() {
    try {
      // Try to decode as Base64 first (recommended approach)
      byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);

      // Validate key length (should be at least 256 bits / 32 bytes for HS256)
      if (keyBytes.length < 32) {
        log.warn("JWT secret is too short. Minimum 256 bits (32 bytes) required. Current: {} bytes", keyBytes.length);
      }

      return Keys.hmacShaKeyFor(keyBytes);
    } catch (Exception e) {
      // Fallback to UTF-8 encoding if Base64 decoding fails
      log.warn("JWT secret is not Base64 encoded. Using UTF-8 bytes as fallback. " +
               "For better security, use a Base64-encoded secret generated with: openssl rand -base64 64");

      byte[] keyBytes = jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);

      // Ensure minimum key length
      if (keyBytes.length < 32) {
        throw new IllegalArgumentException(
          "JWT secret is too short. Must be at least 256 bits (32 bytes). Current: " + keyBytes.length + " bytes. " +
          "Please update JWT_SECRET in your .env file with a proper Base64-encoded secret."
        );
      }

      return Keys.hmacShaKeyFor(keyBytes);
    }
  }

  /**
   * Generate short-lived verification token for email verification
   */
  public String generateVerificationToken(String registrationId, String email, int expiryMinutes) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("registrationId", registrationId);
    claims.put("type", "verification");

    long expiryMillis = expiryMinutes * 60_000L;
    return createToken(claims, email, expiryMillis);
  }

  /**
   * Validate verification token and extract claims
   */
  public Claims validateVerificationToken(String token) {
    Claims claims = extractAllClaims(token);

    // Verify token type
    String type = claims.get("type", String.class);
    if (!"verification".equals(type)) {
      throw new IllegalArgumentException("Invalid token type");
    }

    // Verify not expired
    if (isTokenExpired(token)) {
      throw new IllegalArgumentException("Verification token has expired");
    }

    return claims;
  }
}
