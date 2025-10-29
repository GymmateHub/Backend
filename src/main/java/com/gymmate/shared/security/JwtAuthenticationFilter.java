package com.gymmate.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                UUID userId = tokenProvider.getUserIdFromToken(jwt);
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

                if (!user.isActive()) {
                    handleAuthenticationError(response, "User account is not active");
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
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
            handleAuthenticationError(response, "Authentication failed");
            return;
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

    private void handleAuthenticationError(HttpServletResponse response, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(message));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/actuator");
    }
}
