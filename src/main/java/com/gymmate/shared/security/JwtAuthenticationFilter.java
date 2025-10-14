package com.gymmate.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final TenantAwareUserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain)
    throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      final String jwt = authHeader.substring(7);
      final String userEmail = jwtService.extractUsername(jwt);

      if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        // Load user details with tenant context from token
        TenantAwareUserDetails userDetails = userDetailsService.loadUserByUsernameAndGymId(
          userEmail,
          jwtService.extractGymId(jwt)
        );

        if (jwtService.validateToken(jwt, userDetails)) {
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
          );

          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);

          log.debug("User authenticated: {} for gym: {}", userEmail, userDetails.getGymId());
        }
      }
    } catch (Exception e) {
      log.error("JWT authentication failed: {}", e.getMessage());
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/api/v1/auth/login") ||
      path.startsWith("/api/v1/auth/register") ||
      path.startsWith("/api/v1/gyms/register") ||
      path.startsWith("/actuator/") ||
      path.startsWith("/swagger-ui") ||
      path.startsWith("/v3/api-docs");
  }
}
