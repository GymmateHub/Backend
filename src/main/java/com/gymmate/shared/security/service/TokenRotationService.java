package com.gymmate.shared.security.service;

import com.gymmate.shared.exception.InvalidTokenException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.security.dto.RefreshTokenRequest;
import com.gymmate.shared.security.dto.TokenResponse;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// src/main/java/com/gymmate/shared/security/service/TokenRotationService.java
@Service
@Slf4j
@RequiredArgsConstructor
public class TokenRotationService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final JwtService jwtService;
  private final UserRepository userRepository;

  private static final String REFRESH_TOKEN_FAMILY_PREFIX = "refresh_family:";
  private static final String USED_REFRESH_TOKEN_PREFIX = "used_refresh:";
  private static final long REFRESH_TOKEN_FAMILY_TTL = Duration.ofDays(30).toSeconds();
  private static final long USED_REFRESH_TOKEN_TTL = Duration.ofDays(7).toSeconds();

  @Transactional
  public TokenResponse refreshTokenWithRotation(RefreshTokenRequest request) {
    String refreshToken = request.getRefreshToken();

    // Check if refresh token was already used
    if (isRefreshTokenUsed(refreshToken)) {
      throw new InvalidTokenException("Refresh token has been used and cannot be used again");
    }

    // Validate refresh token
    if (!jwtService.validateToken(refreshToken)) {
      throw new InvalidTokenException("Invalid refresh token");
    }

    // Extract user and family ID
    UUID userId = jwtService.extractUserId(refreshToken);
    String familyId = jwtService.extractClaim(refreshToken, claims -> claims.get("familyId", String.class));

    // If no family ID, this is first time rotation - create family
    if (familyId == null) {
      familyId = UUID.randomUUID().toString();
      createRefreshTokenFamily(familyId, userId);
    } else {
      // Validate family exists and belongs to user
      validateRefreshTokenFamily(familyId, userId);
    }

    // Mark current refresh token as used
    markRefreshTokenAsUsed(refreshToken);

    // Get user and generate new tokens
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

    String newAccessToken = request.getTenantId() != null
      ? jwtService.generateTokenWithFamily(user, request.getTenantId(), familyId)
      : jwtService.generateTokenWithFamily(user, request.getTenantId(), familyId);
    String newRefreshToken = jwtService.generateRefreshTokenWithFamily(user, familyId);

    return TokenResponse.builder()
      .accessToken(newAccessToken)
      .refreshToken(newRefreshToken)
      .build();
  }

  private void createRefreshTokenFamily(String familyId, UUID userId) {
    String key = REFRESH_TOKEN_FAMILY_PREFIX + familyId;
    Map<String, Object> familyData = new HashMap<>();
    familyData.put("userId", userId.toString());
    familyData.put("createdAt", Instant.now().toString());
    familyData.put("lastRotated", Instant.now().toString());

    redisTemplate.opsForHash().putAll(key, familyData);
    redisTemplate.expire(key, Duration.ofSeconds(REFRESH_TOKEN_FAMILY_TTL));
  }

  private void validateRefreshTokenFamily(String familyId, UUID userId) {
    String key = REFRESH_TOKEN_FAMILY_PREFIX + familyId;
    Map<Object, Object> familyData = redisTemplate.opsForHash().entries(key);

    if (familyData.isEmpty()) {
      throw new InvalidTokenException("Invalid refresh token family");
    }

    String storedUserId = (String) familyData.get("userId");
    if (!userId.toString().equals(storedUserId)) {
      throw new InvalidTokenException("Refresh token family validation failed");
    }

    // Update last rotated timestamp
    redisTemplate.opsForHash().put(key, "lastRotated", Instant.now().toString());
  }

  private void markRefreshTokenAsUsed(String refreshToken) {
    String key = USED_REFRESH_TOKEN_PREFIX + refreshToken;
    redisTemplate.opsForValue().set(key, "used", Duration.ofSeconds(USED_REFRESH_TOKEN_TTL));
  }

  private boolean isRefreshTokenUsed(String refreshToken) {
    String key = USED_REFRESH_TOKEN_PREFIX + refreshToken;
    return redisTemplate.hasKey(key);
  }

  public void revokeAllUserTokens(UUID userId) {
    // Find all refresh token families for user and invalidate them
    Set<String> keys = redisTemplate.keys(REFRESH_TOKEN_FAMILY_PREFIX + "*");
    if (keys != null) {
      keys.stream()
        .filter(key -> {
          Map<Object, Object> familyData = redisTemplate.opsForHash().entries(key);
          return userId.toString().equals(familyData.get("userId"));
        })
        .forEach(redisTemplate::delete);
    }
  }
}
