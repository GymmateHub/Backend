package com.gymmate.shared.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoginAttemptService {

  private final RedisTemplate<String, Object> redisTemplate;
  private static final String ATTEMPT_KEY_PREFIX = "login_attempt:";
  private static final String LOCKOUT_KEY_PREFIX = "account_locked:";
  private final long MAX_LOGIN_ATTEMPTS = 5;
  private final long LOCKOUT_DURATION_MINUTES = 30 * 60 * 1000; // 30 minutes

  public void loginFailed(String email) {
    String attemptKey = ATTEMPT_KEY_PREFIX + email.toLowerCase();
    Long attempts = redisTemplate.opsForValue().increment(attemptKey);

    if (attempts != null && attempts >= MAX_LOGIN_ATTEMPTS) {
      lockAccount(email);
      log.warn("Account locked due to too many failed login attempts: {}", email);
    }

    // set expiry for attempt counter
    redisTemplate.expire(attemptKey, Duration.ofMinutes(LOCKOUT_DURATION_MINUTES));
  }

  public void loginSucceeded(String email) {
    redisTemplate.delete(ATTEMPT_KEY_PREFIX + email.toLowerCase());
    redisTemplate.delete(LOCKOUT_KEY_PREFIX + email.toLowerCase());
  }

  public boolean isAccountLocked(String email) {
    String lockoutKey = LOCKOUT_KEY_PREFIX + email.toLowerCase();
    return redisTemplate.hasKey(lockoutKey);
  }

  private void lockAccount(String email) {
    String lockoutKey = LOCKOUT_KEY_PREFIX + email.toLowerCase();
    redisTemplate.opsForValue().set(lockoutKey, "locked", Duration.ofMinutes(LOCKOUT_DURATION_MINUTES));
  }

  public long getRemainingLockoutTime(String email) {
    String lockoutKey = LOCKOUT_KEY_PREFIX + email.toLowerCase();
    Long ttl = redisTemplate.getExpire(lockoutKey, TimeUnit.MINUTES);
    return ttl != null && ttl > 0 ? ttl : 0;
  }

}
