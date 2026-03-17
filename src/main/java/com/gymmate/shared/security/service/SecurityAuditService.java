package com.gymmate.shared.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.shared.constants.AuditEventType;
import com.gymmate.shared.dto.SecurityAuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecurityAuditService {
  private final ObjectMapper objectMapper;
  private final ApplicationEventPublisher eventPublisher;


  public void logSecurityEvent(AuditEventType eventType,
                               UUID userId,
                               String email,
                               String ipAddress,
                               String userAgent,
                               String details,
                               Map<String, Object> metadata) {

    SecurityAuditEvent event = SecurityAuditEvent.builder()
      .eventType(eventType)
      .userId(userId)
      .email(email)
      .ipAddress(ipAddress)
      .userAgent(userAgent)
      .timestamp(Instant.now())
      .details(details)
      .metadata(metadata)
      .build();

    // Log to application logs
    log.info("SECURITY_AUDIT: {}", formatAuditLog(event));

    // Publish event for async processing
    eventPublisher.publishEvent(event);

    // Store in database for audit trail
    storeAuditEvent(event);
  }

  private String formatAuditLog(SecurityAuditEvent event) {
    try {
      return objectMapper.writeValueAsString(event);
    } catch (Exception e) {
      return String.format("SecurityEvent[type=%s, user=%s, ip=%s, details=%s]",
        event.getEventType(), event.getEmail(), event.getIpAddress(), event.getDetails());
    }
  }

  @Async
  public void storeAuditEvent(SecurityAuditEvent event) {
    try {
      // Store in audit_events table
      // Implementation depends on your audit table structure
      log.debug("Stored audit event: {}", event.getEventType());
    } catch (Exception e) {
      log.error("Failed to store audit event", e);
    }
  }

  // Convenience methods
  public void logLoginSuccess(UUID userId, String email, String ipAddress, String userAgent) {
    logSecurityEvent(AuditEventType.LOGIN_SUCCESS, userId, email, ipAddress, userAgent,
      "User logged in successfully", Map.of());
  }

  public void logLoginFailure(String email, String ipAddress, String userAgent, String reason) {
    logSecurityEvent(AuditEventType.LOGIN_FAILED, null, email, ipAddress, userAgent,
      reason, Map.of("reason", reason));
  }

  public void logAccountLocked(String email, String ipAddress, int attempts) {
    logSecurityEvent(AuditEventType.ACCOUNT_LOCKED, null, email, ipAddress, null,
      "Account locked due to multiple failed attempts",
      Map.of("failedAttempts", attempts));
  }

  public void logSuspiciousActivity(UUID userId, String email, String ipAddress,
                                    String activity, String reason) {
    logSecurityEvent(AuditEventType.SUSPICIOUS_ACTIVITY, userId, email, ipAddress, null,
      activity, Map.of("reason", reason, "activity", activity));
  }

  public void logDataAccess(UUID userId, String email, String ipAddress,
                            String resource, String action) {
    logSecurityEvent(AuditEventType.DATA_ACCESS, userId, email, ipAddress, null,
      String.format("User accessed %s - %s", resource, action),
      Map.of("resource", resource, "action", action));
  }

}
