package com.gymmate.shared.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TotpService {

  private static final int OTP_LENGTH = 6;
  private static final long OTP_VALIDITY_SECONDS = 300; // 5 minutes
  private static final long RATE_LIMIT_SECONDS = 60; // 1 minute between resends
  private static final int MAX_OTP_ATTEMPTS = 5;
  private static final String OTP_KEY_PREFIX = "otp:";
  private static final String RATE_LIMIT_KEY_PREFIX = "otp_rate:";
  private static final String OTP_ATTEMPTS_KEY_PREFIX = "otp_attempts:";

  // Lua script for atomic rate limit check and update
  private static final String RATE_LIMIT_LUA_SCRIPT = 
      "local key = KEYS[1] " +
      "local rate_limit_millis = tonumber(ARGV[1]) " +
      "local current_time = tonumber(ARGV[2]) " +
      "local last_sent = redis.call('GET', key) " +
      "if last_sent == false then " +
      "  redis.call('SET', key, current_time, 'PX', rate_limit_millis) " +
      "  return 1 " +
      "end " +
      "local time_since_last = current_time - tonumber(last_sent) " +
      "if time_since_last >= rate_limit_millis then " +
      "  redis.call('SET', key, current_time, 'PX', rate_limit_millis) " +
      "  return 1 " +
      "end " +
      "return 0";

  private final RedisTemplate<String, Object> redisTemplate;
  private final SecureRandom secureRandom = new SecureRandom();
  private final DefaultRedisScript<Long> rateLimitScript;

  public TotpService(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.rateLimitScript = new DefaultRedisScript<>();
    this.rateLimitScript.setScriptText(RATE_LIMIT_LUA_SCRIPT);
    this.rateLimitScript.setResultType(Long.class);
  }

  /**
   * Generate a new OTP for the given registration ID
   */
  public String generateOtp(String registrationId) {
    String otp = String.format("%06d", secureRandom.nextInt(1000000));
    String key = OTP_KEY_PREFIX + registrationId;

    Map<String, Object> otpData = new HashMap<>();
    otpData.put("otp", otp);
    otpData.put("expiresAt", Instant.now().plusSeconds(OTP_VALIDITY_SECONDS).toEpochMilli());
    otpData.put("attempts", 0);

    redisTemplate.opsForValue().set(key, otpData, Duration.ofSeconds(OTP_VALIDITY_SECONDS));

    log.debug("Generated OTP for registrationId: {}", registrationId);
    return otp;
  }

  /**
   * Verify the OTP for the given registration ID
   */
  public boolean verifyOtp(String registrationId, String otp) {
    String key = OTP_KEY_PREFIX + registrationId;

    @SuppressWarnings("unchecked")
    Map<String, Object> otpData = (Map<String, Object>) redisTemplate.opsForValue().get(key);

    if (otpData == null) {
      log.warn("No OTP found for registrationId: {}", registrationId);
      return false;
    }

    // Check if OTP has expired
    long expiresAt = ((Number) otpData.get("expiresAt")).longValue();
    if (Instant.now().toEpochMilli() > expiresAt) {
      log.warn("OTP expired for registrationId: {}", registrationId);
      invalidateOtp(registrationId);
      return false;
    }

    // Check attempts
    int attempts = ((Number) otpData.getOrDefault("attempts", 0)).intValue();
    if (attempts >= MAX_OTP_ATTEMPTS) {
      log.warn("Max OTP attempts reached for registrationId: {}", registrationId);
      invalidateOtp(registrationId);
      return false;
    }

    // Verify OTP
    String storedOtp = (String) otpData.get("otp");
    if (storedOtp.equals(otp)) {
      log.info("OTP verified successfully for registrationId: {}", registrationId);
      invalidateOtp(registrationId); // One-time use
      return true;
    }

    // Increment attempts
    otpData.put("attempts", attempts + 1);
    long ttl = expiresAt - Instant.now().toEpochMilli();
    if (ttl > 0) {
      redisTemplate.opsForValue().set(key, otpData, Duration.ofMillis(ttl));
    }

    log.warn("Invalid OTP attempt for registrationId: {}", registrationId);
    return false;
  }

  /**
   * Invalidate the OTP (used after verification or expiry)
   */
  public void invalidateOtp(String registrationId) {
    String key = OTP_KEY_PREFIX + registrationId;
    redisTemplate.delete(key);
    log.debug("Invalidated OTP for registrationId: {}", registrationId);
  }

  /**
   * Atomically check if rate limit allows sending OTP and update the timestamp if allowed.
   * This prevents race conditions by using a Lua script to perform both operations atomically.
   * 
   * @param registrationId The registration ID
   * @return true if OTP can be sent (and rate limit was updated), false otherwise
   */
  public boolean checkAndUpdateRateLimit(String registrationId) {
    String rateLimitKey = RATE_LIMIT_KEY_PREFIX + registrationId;
    long currentTimeMillis = Instant.now().toEpochMilli();
    long rateLimitMillis = RATE_LIMIT_SECONDS * 1000;
    
    Long result = redisTemplate.execute(
        rateLimitScript,
        Collections.singletonList(rateLimitKey),
        rateLimitMillis,
        currentTimeMillis
    );
    
    boolean allowed = result != null && result == 1L;
    log.debug("Rate limit check for registrationId {}: {}", registrationId, allowed ? "allowed" : "blocked");
    return allowed;
  }

  /**
   * Check if rate limit allows sending OTP (non-atomic, for read-only checks)
   * @deprecated Use checkAndUpdateRateLimit() for atomic check-and-set operations
   */
  @Deprecated
  public boolean canSendOtp(String registrationId) {
    String rateLimitKey = RATE_LIMIT_KEY_PREFIX + registrationId;
    Long lastSentAt = (Long) redisTemplate.opsForValue().get(rateLimitKey);

    if (lastSentAt == null) {
      return true;
    }

    long secondsSinceLastSent = Instant.now().toEpochMilli() - lastSentAt;
    return secondsSinceLastSent >= (RATE_LIMIT_SECONDS * 1000);
  }

  /**
   * Get remaining seconds before next OTP can be sent
   */
  public long getRemainingRateLimitSeconds(String registrationId) {
    String rateLimitKey = RATE_LIMIT_KEY_PREFIX + registrationId;
    Long lastSentAt = (Long) redisTemplate.opsForValue().get(rateLimitKey);

    if (lastSentAt == null) {
      return 0;
    }

    long millisSinceLastSent = Instant.now().toEpochMilli() - lastSentAt;
    long remainingMillis = (RATE_LIMIT_SECONDS * 1000) - millisSinceLastSent;

    return Math.max(0, remainingMillis / 1000);
  }

  /**
   * Update rate limit timestamp
   */
  public void updateRateLimit(String registrationId) {
    String rateLimitKey = RATE_LIMIT_KEY_PREFIX + registrationId;
    redisTemplate.opsForValue().set(
        rateLimitKey,
        Instant.now().toEpochMilli(),
        Duration.ofSeconds(RATE_LIMIT_SECONDS)
    );
    log.debug("Updated rate limit for registrationId: {}", registrationId);
  }

  /**
   * Get remaining OTP attempts
   */
  public int getRemainingAttempts(String registrationId) {
    String key = OTP_KEY_PREFIX + registrationId;

    @SuppressWarnings("unchecked")
    Map<String, Object> otpData = (Map<String, Object>) redisTemplate.opsForValue().get(key);

    if (otpData == null) {
      return MAX_OTP_ATTEMPTS;
    }

    int attempts = ((Number) otpData.getOrDefault("attempts", 0)).intValue();
    return Math.max(0, MAX_OTP_ATTEMPTS - attempts);
  }
}

