package com.gymmate.shared.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1) // Ensure this filter runs before authentication filters
@Slf4j
public class SecurityHeadersFilter extends OncePerRequestFilter {
  @Value("${app.security.csp.enabled:true}")
  private boolean cspEnabled;

  @Value("${app.security.hsts.enabled:true}")
  private boolean hstsEnabled;

  @Value("${app.frontend-url:http://localhost:3000}")
  private String frontendUrl;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    // Content Security Policy
    if (cspEnabled) {
      String csp = String.format(
        "default-src 'self'; " +
          "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
          "style-src 'self' 'unsafe-inline'; " +
          "img-src 'self' data: https:; " +
          "font-src 'self'; " +
          "connect-src 'self' %s; " +
          "frame-ancestors 'none'; " +
          "base-uri 'self'; " +
          "form-action 'self'",
        frontendUrl
      );
      response.setHeader("Content-Security-Policy", csp);
    }

    // HTTP Strict Transport Security
    if (hstsEnabled && request.isSecure()) {
      response.setHeader("Strict-Transport-Security",
        "max-age=31536000; includeSubDomains; preload");
    }

    // Other security headers
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("X-Frame-Options", "DENY");
    response.setHeader("X-XSS-Protection", "1; mode=block");
    response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    response.setHeader("Permissions-Policy",
      "geolocation=(), microphone=(), camera=(), payment=(), usb=()");

    // Remove server information
    response.setHeader("Server", "");

    filterChain.doFilter(request, response);
  }
}
