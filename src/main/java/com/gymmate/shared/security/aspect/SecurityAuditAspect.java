package com.gymmate.shared.security.aspect;

import com.gymmate.shared.security.TenantAwareUserDetails;
import com.gymmate.shared.security.service.SecurityAuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class SecurityAuditAspect {

  private final SecurityAuditService auditService;

  @AfterReturning(pointcut = "@annotation(auditLog)", returning = "result")
  public void auditSuccess(JoinPoint joinPoint, AuditLog auditLog, Object result) {
    HttpServletRequest request = getCurrentRequest();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    UUID userId = extractUserId(auth);
    String email = extractEmail(auth);
    String ipAddress = getClientIpAddress(request);
    String userAgent = request != null ? request.getHeader("User-Agent") : null;

    Map<String, Object> metadata = new HashMap<>();
    metadata.put("method", joinPoint.getSignature().getName());
    if (result != null) {
      metadata.put("result", result);
    }

    auditService.logSecurityEvent(
      auditLog.eventType(),
      userId,
      email,
      ipAddress,
      userAgent,
      auditLog.message(),
      metadata
    );
  }

  @AfterThrowing(pointcut = "@annotation(auditLog)", throwing = "exception")
  public void auditFailure(JoinPoint joinPoint, AuditLog auditLog, Exception exception) {
    HttpServletRequest request = getCurrentRequest();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    UUID userId = extractUserId(auth);
    String email = extractEmail(auth);
    String ipAddress = getClientIpAddress(request);
    String userAgent = request != null ? request.getHeader("User-Agent") : null;

    String errorMessage = exception.getMessage() != null ? exception.getMessage() : exception.getClass().getSimpleName();

    Map<String, Object> metadata = new HashMap<>();
    metadata.put("method", joinPoint.getSignature().getName());
    metadata.put("error", errorMessage);

    auditService.logSecurityEvent(
      auditLog.eventType(),
      userId,
      email,
      ipAddress,
      userAgent,
      auditLog.message() + " - Failed: " + errorMessage,
      metadata
    );
  }

  private HttpServletRequest getCurrentRequest() {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    return attributes instanceof ServletRequestAttributes ?
      ((ServletRequestAttributes) attributes).getRequest() : null;
  }

  private UUID extractUserId(Authentication auth) {
    if (auth != null && auth.getPrincipal() instanceof TenantAwareUserDetails) {
      return ((TenantAwareUserDetails) auth.getPrincipal()).getUserId();
    }
    return null;
  }

  private String extractEmail(Authentication auth) {
    if (auth != null && auth.getPrincipal() instanceof TenantAwareUserDetails) {
      return ((TenantAwareUserDetails) auth.getPrincipal()).getUsername();
    }
    return null;
  }

  private String getClientIpAddress(HttpServletRequest request) {
    if (request == null) return null;

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
}
