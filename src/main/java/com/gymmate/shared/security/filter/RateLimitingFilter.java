package com.gymmate.shared.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.security.service.JwtService;
import com.gymmate.shared.security.service.RateLimitingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// src/main/java/com/gymmate/shared/security/filter/RateLimitingFilter.java
@Component
@Order(2)
@Slf4j
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

  private final RateLimitingService rateLimitingService;
  private final ObjectMapper objectMapper;
  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    String endpoint = request.getRequestURI();
    String clientIp = getClientIpAddress(request);
    String identifier = getIdentifier(request);

    if (!rateLimitingService.isAllowed(identifier, endpoint, clientIp)) {
      handleRateLimitExceeded(response, endpoint, identifier, clientIp);
      return;
    }

    // Add rate limit headers
    long remaining = rateLimitingService.getRemainingRequests(identifier, endpoint, clientIp);
    response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
    response.setHeader("X-RateLimit-Limit", "100");

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/actuator/health") ||
      path.startsWith("/actuator/info") ||
      path.startsWith("/swagger-ui") ||
      path.startsWith("/v3/api-docs");
  }

  private String getIdentifier(HttpServletRequest request) {
    // Try to get user ID from JWT
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      try {
        String token = authHeader.substring(7);
        // Extract user ID from token (simplified)
        return extractUserIdFromToken(token);
      } catch (Exception e) {
        log.debug("Failed to extract user ID from token");
      }
    }

    // Fall back to IP address
    return getClientIpAddress(request);
  }

  private String getClientIpAddress(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }

    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }

    return request.getRemoteAddr();
  }

  private void handleRateLimitExceeded(HttpServletResponse response,
                                       String endpoint,
                                       String identifier,
                                       String clientIp) throws IOException {
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

    ApiResponse<?> apiResponse = ApiResponse.error(
      "Rate limit exceeded. Please try again later.");

    objectMapper.writeValue(response.getWriter(), apiResponse);

    log.warn("Rate limit exceeded - Endpoint: {}, Identifier: {}, IP: {}",
      endpoint, identifier, clientIp);
  }

  private String extractUserIdFromToken(String token) {
    try {
      return jwtService.extractUserId(token).toString();
    } catch (Exception e) {
      log.debug("Failed to extract user ID from token for rate limiting");
      return null;
    }
  }
}
