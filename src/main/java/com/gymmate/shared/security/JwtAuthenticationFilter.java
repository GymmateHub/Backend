package com.gymmate.shared.security;

import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // Only validate and process if JWT is present
                if (tokenProvider.validateToken(jwt)) {
                    UUID userId = tokenProvider.getUserIdFromToken(jwt);
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

                    if (!user.isActive()) {
                        log.warn("Inactive user attempted to authenticate: {}", userId);
                        SecurityContextHolder.clearContext();
                        filterChain.doFilter(request, response);
                        return;
                    }

                    TenantAwareUserDetails userDetails = new TenantAwareUserDetails(user);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Set tenant context for SUPER_ADMIN or if user has a gym association
                    if (user.getGymId() != null) {
                        TenantContext.setCurrentTenantId(user.getGymId());
                    }
                } else {
                    // Invalid token - clear security context and let Spring Security handle it
                    log.warn("Invalid JWT token for request: {} {}", request.getMethod(), request.getRequestURI());
                    SecurityContextHolder.clearContext();
                }
            }
            // If no JWT token, just continue - anonymous access will be checked by SecurityFilterChain
        } catch (Exception ex) {
            log.error("Error processing JWT authentication: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Allow these paths to skip JWT processing entirely
        return path.equals("/") ||
               path.equals("/error") ||
               path.startsWith("/api/auth/") ||
               path.equals("/api/gyms/register") ||
               path.equals("/api/users/register/gym-owner") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/webjars/") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/actuator/info");
    }
}
