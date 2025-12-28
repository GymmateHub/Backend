package com.gymmate.shared.multitenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.shared.dto.ApiResponse;
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

    // Endpoints that don't require tenant context
    private static final List<String> NON_TENANT_ENDPOINTS = Arrays.asList(
        "/api/auth",
        "/api/gyms",
        "/api/gyms/register",
        "/api/users/register",
        "/v3/api-docs",
        "/swagger-ui",
        "/actuator"
    );

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
                UUID tenantId = userDetails.getGymId();

                if (tenantId != null) {
                    log.debug("Setting tenant context for gym: {}", tenantId);
                    TenantContext.setCurrentTenantId(tenantId);
                    filterChain.doFilter(request, response);
                } else {
                    log.warn("Authenticated user {} has no gym/tenant assigned", userDetails.getUsername());
                    handleNoTenantError(response);
                }
            } else {
                // For unauthenticated or non-tenant-aware requests, proceed without tenant context
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

    private void handleNoTenantError(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ApiResponse<?> apiResponse = ApiResponse.error("No tenant context available");
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
