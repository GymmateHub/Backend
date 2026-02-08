package com.gymmate.shared.multitenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.security.service.JwtService;
import com.gymmate.shared.security.TenantAwareUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    // Endpoints that don't require tenant context
    private static final List<String> NON_TENANT_ENDPOINTS = Arrays.asList(
            "/api/auth",
            "/api/gyms/register",
            "/api/gyms/my-gyms",
            "/api/gyms/active",
            "/api/gyms/city",
            "/api/organisations/current",
            "/api/users/register",
            "/api/users/verify-otp",
            "/api/users/resend-otp",
            "/v3/api-docs",
            "/swagger-ui",
            "/actuator");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            // Skip tenant filtering for public/non-tenant endpoints
            if (shouldSkipTenantFilter(request)) {
                log.debug("Skipping tenant filter for endpoint: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // If authenticated, try to set tenant context
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof TenantAwareUserDetails) {

                TenantAwareUserDetails userDetails = (TenantAwareUserDetails) authentication.getPrincipal();

                // SUPER_ADMIN can bypass tenant context requirement
                if (isSuperAdmin(userDetails)) {
                    log.debug("SUPER_ADMIN user {} bypassing tenant context requirement", userDetails.getUsername());
                    filterChain.doFilter(request, response);
                    return;
                }

                UUID organisationId = userDetails.getOrganisationId();

                if (organisationId != null) {
                    log.debug("Setting tenant context for organisation: {}", organisationId);
                    TenantContext.setCurrentTenantId(organisationId);

                    // Try to extract gym context from JWT if present
                    String authHeader = request.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        UUID gymId = jwtService.extractGymId(token);
                        if (gymId != null) {
                            TenantContext.setCurrentGymId(gymId);
                            log.debug("Setting gym context: {}", gymId);
                        }
                    }

                    filterChain.doFilter(request, response);
                } else {
                    log.warn("Authenticated user {} has no organisation/tenant assigned", userDetails.getUsername());
                    handleNoTenantError(response, userDetails.getUsername());
                }
            } else {
                // For unauthenticated or non-tenant-aware requests, proceed without tenant
                // context
                log.debug("No tenant-aware authentication found, proceeding without tenant context");
                filterChain.doFilter(request, response);
            }
        } finally {
            TenantContext.clear();
        }
    }

    private boolean shouldSkipTenantFilter(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        return NON_TENANT_ENDPOINTS.stream().anyMatch(requestPath::startsWith);
    }

    private void handleNoTenantError(HttpServletResponse response, String username) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        String message = String.format(
                "Organisation context required. User '%s' is not associated with any organisation. " +
                        "Please contact support or complete organisation setup.",
                username);

        ApiResponse<?> apiResponse = ApiResponse.error(message);
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }

    private boolean isSuperAdmin(TenantAwareUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN")
                        || auth.getAuthority().equals("SUPER_ADMIN"));
    }
}
