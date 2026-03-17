package com.gymmate.shared.security.service;

import com.gymmate.subscription.config.RateLimitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimitingService {

  private final StringRedisTemplate stringRedisTemplate;

  private static final String RATE_LIMIT_PREFIX = "rate_limit:";
  private static final String IP_RATE_LIMIT_PREFIX = "ip_rate_limit:";

  // Rate limit configurations
  private static final Map<String, RateLimitConfig> RATE_LIMITS = Map.of(
    "/api/auth/login", new RateLimitConfig(5, Duration.ofMinutes(15)), // 5 attempts per 15 min
    "/api/auth/register", new RateLimitConfig(3, Duration.ofMinutes(60)), // 3 registrations per hour
    "/api/auth/forgot-password", new RateLimitConfig(3, Duration.ofMinutes(60)), // 3 password resets per hour
    "/api/auth/verify-otp", new RateLimitConfig(10, Duration.ofMinutes(5)), // 10 OTP attempts per 5 min
    "/api/auth/resend-otp", new RateLimitConfig(3, Duration.ofMinutes(5)), // 3 resends per 5 min
    "default", new RateLimitConfig(100, Duration.ofMinutes(1)), // 100 requests per minute
    "upload", new RateLimitConfig(10, Duration.ofMinutes(5)) // 10 uploads per 5 minutes
  );

  public boolean isAllowed(String identifier, String endpoint, String clientIp) {
    RateLimitConfig config = getRateLimitConfig(endpoint);

    // Check both user-specific and IP-based limits
    boolean userAllowed = checkRateLimit(RATE_LIMIT_PREFIX + identifier, config);
    boolean ipAllowed = checkRateLimit(IP_RATE_LIMIT_PREFIX + clientIp, config);

    return userAllowed && ipAllowed;
  }

  private boolean checkRateLimit(String key, RateLimitConfig config) {
    try {
      String luaScript = """
                local key = KEYS[1]
                local limit = tonumber(ARGV[1])
                local window = tonumber(ARGV[2])
                local current_time = tonumber(ARGV[3])

                local bucket_key = key .. ':' .. math.floor(current_time / window)
                local current = redis.call('GET', bucket_key)

                if current == false then
                    redis.call('SET', bucket_key, 1)
                    redis.call('EXPIRE', bucket_key, window)
                    return 1
                end

                if tonumber(current) < limit then
                    redis.call('INCR', bucket_key)
                    return 1
                end

                return 0
                """;

      DefaultRedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);
      Long result = stringRedisTemplate.execute(script,
        Collections.singletonList(key),
        String.valueOf(config.limit()),
        String.valueOf(config.window().toSeconds()),
        String.valueOf(System.currentTimeMillis() / 1000));

      return result != null && result == 1;
    } catch (Exception e) {
      log.error("Rate limiting check failed for key: {}", key, e);
      return true; // Allow on failure
    }
  }

  private RateLimitConfig getRateLimitConfig(String endpoint) {
    // Check for exact match first
    RateLimitConfig config = RATE_LIMITS.get(endpoint);
    if (config != null) return config;

    // Check for pattern matches
    if (endpoint.contains("/upload")) return RATE_LIMITS.get("upload");

    // Return default
    return RATE_LIMITS.get("default");
  }

  public long getRemainingRequests(String identifier, String endpoint, String clientIp) {
    RateLimitConfig config = getRateLimitConfig(endpoint);
    String key = RATE_LIMIT_PREFIX + identifier;

    try {
      String current = stringRedisTemplate.opsForValue().get(key);
      if (current == null) return config.limit();
      return Math.max(0, config.limit() - Long.parseLong(current));
    } catch (Exception e) {
      log.error("Failed to get remaining requests for key: {}", key, e);
      return 0;
    }
  }

  public record RateLimitConfig(int limit, Duration window) {}
}
