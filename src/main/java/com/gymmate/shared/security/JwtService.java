package com.gymmate.shared.security;

 import com.gymmate.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

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
    claims.put("gymId", userDetails.getGymId().toString());
    claims.put("role", userDetails.getRole());
    claims.put("emailVerified", userDetails.isEmailVerified());

    return createToken(claims, userDetails.getUsername(), jwtExpiration);
  }

  /**
   * Generate refresh token
   */
  public String generateRefreshToken(TenantAwareUserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userDetails.getUserId().toString());
    claims.put("gymId", userDetails.getGymId().toString());

    return createToken(claims, userDetails.getUsername(), refreshExpiration);
  }

  /**
   * Generate JWT token directly from User entity with specific tenant/gym context
   */
  public String generateToken(User user, UUID gymId) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId().toString());
    claims.put("gymId", gymId != null ? gymId.toString() : null);
    claims.put("email", user.getEmail());
    claims.put("role", user.getRole().name());

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

    return createToken(claims, user.getEmail(), refreshExpiration);
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
  public String extractUsername(String token) {
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
      .verifyWith(getSigningKey())
      .build()
      .parseSignedClaims(token)
      .getPayload();
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
    final String username = extractUsername(token);
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
      extractAllClaims(token);
      return !isTokenExpired(token);
    } catch (Exception e) {
      log.error("Token validation failed: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Get signing key from secret
   */
  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes());
  }
}
