package com.gymmate.shared.multitenancy;

import com.gymmate.shared.security.TenantAwareUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
    throws ServletException, IOException {
    try {
      UUID tenantId = extractTenantId(request);

      if (tenantId != null) {
        TenantContext.setCurrentTenantId(tenantId);
        log.debug("Tenant context set to: {}", tenantId);
      }

      filterChain.doFilter(request, response);
    } finally {
      TenantContext.clear();
    }
  }

  private UUID extractTenantId(HttpServletRequest request) {
    // Option 1: From custom header (useful for testing and public endpoints)
    String tenantHeader = request.getHeader("X-Tenant-ID");
    if (tenantHeader != null && !tenantHeader.isBlank()) {
      try {
        return UUID.fromString(tenantHeader);
      } catch (IllegalArgumentException e) {
        log.warn("Invalid tenant ID in header: {}", tenantHeader);
      }
    }

    // Option 2: From authenticated user (production - after JWT authentication)
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof TenantAwareUserDetails) {
      TenantAwareUserDetails userDetails = (TenantAwareUserDetails) auth.getPrincipal();
      return userDetails.getTenantId();
    }

    return null;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    // Don't apply tenant filter to public endpoints
    return path.startsWith("/api/v1/auth/login") ||
      path.startsWith("/api/v1/auth/register") ||
      path.startsWith("/api/v1/gyms/register") ||
      path.startsWith("/actuator/") ||
      path.startsWith("/swagger-ui") ||
      path.startsWith("/v3/api-docs");
  }
}
