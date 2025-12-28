package com.gymmate.subscription.application;

import com.gymmate.shared.multitenancy.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Skip rate limiting for health checks and public endpoints
        String path = request.getRequestURI();
        if (isExcludedPath(path)) {
            return true;
        }

        // Get gym ID from tenant context
        UUID gymId = TenantContext.getCurrentTenantId();
        if (gymId == null) {
            // No tenant context, likely a super admin or public endpoint
            return true;
        }

        String endpoint = path;
        String ipAddress = getClientIp(request);

        // Check both hourly and burst limits
        boolean hourlyAllowed = rateLimitService.checkRateLimit(gymId, "hourly", endpoint, ipAddress);
        boolean burstAllowed = rateLimitService.checkRateLimit(gymId, "burst", endpoint, ipAddress);

        if (!hourlyAllowed || !burstAllowed) {
            RateLimitStatus status = rateLimitService.getRateLimitStatus(gymId);

            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");

            // Add rate limit headers
            response.setHeader("X-RateLimit-Limit-Hourly", status.getHourlyLimit().toString());
            response.setHeader("X-RateLimit-Remaining-Hourly", status.getHourlyRemaining().toString());
            response.setHeader("X-RateLimit-Limit-Burst", status.getBurstLimit().toString());
            response.setHeader("X-RateLimit-Remaining-Burst", status.getBurstRemaining().toString());

            if (status.getBlockedUntil() != null) {
                response.setHeader("Retry-After", String.valueOf(
                    java.time.Duration.between(java.time.LocalDateTime.now(), status.getBlockedUntil()).getSeconds()
                ));
            }

            String errorMessage = String.format(
                "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\",\"tierName\":\"%s\"}",
                status.getTierName()
            );
            response.getWriter().write(errorMessage);

            return false;
        }

        // Add rate limit info to response headers
        RateLimitStatus status = rateLimitService.getRateLimitStatus(gymId);
        response.setHeader("X-RateLimit-Limit-Hourly", status.getHourlyLimit().toString());
        response.setHeader("X-RateLimit-Remaining-Hourly", status.getHourlyRemaining().toString());
        response.setHeader("X-RateLimit-Limit-Burst", status.getBurstLimit().toString());
        response.setHeader("X-RateLimit-Remaining-Burst", status.getBurstRemaining().toString());

        return true;
    }

    private boolean isExcludedPath(String path) {
        return path.startsWith("/actuator") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/") ||
               path.startsWith("/auth/") ||
               path.startsWith("/public/");
    }

    private String getClientIp(HttpServletRequest request) {
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

