package com.gymmate.shared.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

// src/main/java/com/gymmate/shared/security/service/PasswordPolicyService.java
@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordPolicyService {

  private final RedisTemplate<String, Object> redisTemplate;
  private static final String PASSWORD_HISTORY_PREFIX = "password_history:";
  private static final int PASSWORD_HISTORY_SIZE = 12;
  private static final int MIN_PASSWORD_LENGTH = 12;

  public PasswordValidationResult validatePassword(String password, UUID userId) {
    List<String> errors = new ArrayList<>();

    // Length check
    if (password.length() < MIN_PASSWORD_LENGTH) {
      errors.add(String.format("Password must be at least %d characters long", MIN_PASSWORD_LENGTH));
    }

    // Complexity checks
    if (!password.matches(".*[A-Z].*")) {
      errors.add("Password must contain at least one uppercase letter");
    }

    if (!password.matches(".*[a-z].*")) {
      errors.add("Password must contain at least one lowercase letter");
    }

    if (!password.matches(".*\\d.*")) {
      errors.add("Password must contain at least one digit");
    }

    if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
      errors.add("Password must contain at least one special character");
    }

    // Common password check
    if (isCommonPassword(password)) {
      errors.add("Password is too common. Please choose a more secure password");
    }

    // Sequential character check
    if (hasSequentialChars(password)) {
      errors.add("Password cannot contain sequential characters (e.g., 'abc', '123')");
    }

    // Repeated character check
    if (hasRepeatedChars(password)) {
      errors.add("Password cannot contain repeated characters (e.g., 'aaa', '111')");
    }

    // Password history check
    if (userId != null && isInPasswordHistory(password, userId)) {
      errors.add("Password cannot be reused. Please choose a different password");
    }

    return new PasswordValidationResult(errors.isEmpty(), errors);
  }

  public void addToPasswordHistory(UUID userId, String hashedPassword) {
    String key = PASSWORD_HISTORY_PREFIX + userId;

    // Add to list and trim to required size
    redisTemplate.opsForList().leftPush(key, hashedPassword);
    redisTemplate.opsForList().trim(key, 0, PASSWORD_HISTORY_SIZE - 1);
    redisTemplate.expire(key, Duration.ofDays(365 * 2)); // Keep for 2 years
  }

  private boolean isInPasswordHistory(String password, UUID userId) {
    String key = PASSWORD_HISTORY_PREFIX + userId;
    List<Object> history = redisTemplate.opsForList().range(key, 0, -1);

    if (history != null) {
      PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
      return history.stream()
        .anyMatch(oldHash -> passwordEncoder.matches(password, (String) oldHash));
    }

    return false;
  }

  private boolean isCommonPassword(String password) {
    // Load common passwords list (simplified - in production, use a comprehensive list)
    List<String> commonPasswords = Arrays.asList(
      "password", "123456", "password123", "admin", "qwerty",
      "letmein", "welcome", "monkey", "1234567890", "password1"
    );

    return commonPasswords.contains(password.toLowerCase());
  }

  private boolean hasSequentialChars(String password) {
    String lower = password.toLowerCase();

    // Check for sequential letters
    for (int i = 0; i < lower.length() - 2; i++) {
      char c1 = lower.charAt(i);
      char c2 = lower.charAt(i + 1);
      char c3 = lower.charAt(i + 2);

      if (c2 == c1 + 1 && c3 == c2 + 1) {
        return true;
      }
    }

    return false;
  }

  private boolean hasRepeatedChars(String password) {
    for (int i = 0; i < password.length() - 2; i++) {
      char c = password.charAt(i);
      if (password.charAt(i + 1) == c && password.charAt(i + 2) == c) {
        return true;
      }
    }
    return false;
  }

  public record PasswordValidationResult(boolean valid, List<String> errors) {}
}
