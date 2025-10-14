package com.gymmate.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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
   * Create token with claims and expiration
   */
  private String createToken(Map<String, Object> claims, String subject, long expiration) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
      .setClaims(claims)
      .setSubject(subject)
      .setIssuedAt(now)
      .setExpiration(expiryDate)
      .signWith(getSigningKey(), SignatureAlgorithm.HS256)
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
    return UUID.fromString(extractClaim(token, claims -> claims.get("userId", String.class)));
  }

  /**
   * Extract gym ID (tenant ID) from token
   */
  public UUID extractGymId(String token) {
    return UUID.fromString(extractClaim(token, claims -> claims.get("gymId", String.class)));
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
  private Key getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes());
  }
}
