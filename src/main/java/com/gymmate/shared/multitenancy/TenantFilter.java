package com.gymmate.shared.multitenancy;

import tools.jackson.databind.ObjectMapper;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.security.TenantAwareUserDetails;
import com.gymmate.shared.security.audit.SecurityAuditService;
import com.gymmate.shared.security.Permission;
import com.gymmate.shared.constants.AuditEventType;
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

    /** Header a SUPER_ADMIN sends to act inside an organisation; ignored for everyone else. */
    public static final String ORGANISATION_OVERRIDE_HEADER = "X-Organisation-Id";

    private final ObjectMapper objectMapper;
    private final SecurityAuditService securityAuditService;

    // Endpoints that don't require tenant context
    // BUG-033: "/api/users/register", "/api/users/verify-otp", "/api/users/resend-otp" were
    // removed — no such routes exist on UserController (real routes are
    // /api/auth/register/*, already covered by the "/api/auth" prefix below); they were
    // leftover from a prior route rename.
    private static final List<String> NON_TENANT_ENDPOINTS = Arrays.asList(
            "/api/auth",
            "/api/webhooks",
            "/api/gyms/register",
            "/api/gyms/my-gyms",
            "/api/gyms/active",
            "/api/gyms/city",
            "/api/organisations/current",
            "/v3/api-docs",
            "/scalar.html",
            "/actuator");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            if (applySuperAdminOverride(request)) {
                filterChain.doFilter(request, response);
                return;
            }

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

                    // Gym context: the gym claim of the validated access token (carried on the
                    // principal by the JWT filter), else the X-Gym-Id header
                    UUID gymId = userDetails.getGymId();
                    if (gymId == null) {
                        String gymHeader = request.getHeader("X-Gym-Id");
                        if (org.springframework.util.StringUtils.hasText(gymHeader)) {
                            try {
                                gymId = UUID.fromString(gymHeader);
                            } catch (IllegalArgumentException ignored) {}
                        }
                    }
                    if (gymId != null) {
                        TenantContext.setCurrentGymId(gymId);
                        log.debug("Setting gym context: {}", gymId);
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

    /**
     * A SUPER_ADMIN acts inside an organisation by sending {@value #ORGANISATION_OVERRIDE_HEADER}
     * (and optionally X-Gym-Id). Every such request is written to the security audit log.
     *
     * @return true when the override was applied
     */
    private boolean applySuperAdminOverride(HttpServletRequest request) {
        String header = request.getHeader(ORGANISATION_OVERRIDE_HEADER);
        if (header == null || header.isBlank()) {
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof TenantAwareUserDetails user)
                || !isSuperAdmin(user)) {
            log.debug("Ignoring {} from a non-platform-admin caller", ORGANISATION_OVERRIDE_HEADER);
            return false;
        }
        UUID organisationId;
        try {
            organisationId = UUID.fromString(header.trim());
        } catch (IllegalArgumentException e) {
            return false;
        }
        UUID gymId = null;
        String gymHeader = request.getHeader("X-Gym-Id");
        if (org.springframework.util.StringUtils.hasText(gymHeader)) {
            try {
                gymId = UUID.fromString(gymHeader.trim());
            } catch (IllegalArgumentException ignored) {
                // no gym context
            }
        }
        TenantContext.setCurrentTenantId(organisationId);
        if (gymId != null) {
            TenantContext.setCurrentGymId(gymId);
        }
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("organisationId", organisationId.toString());
        metadata.put("method", request.getMethod());
        metadata.put("path", request.getRequestURI());
        if (gymId != null) {
            metadata.put("gymId", gymId.toString());
        }
        securityAuditService.logSecurityEvent(AuditEventType.SUPER_ADMIN_ORG_OVERRIDE, user.getUserId(), user.getEmail(),
                request.getRemoteAddr(), request.getHeader("User-Agent"),
                request.getMethod() + " " + request.getRequestURI() + " as organisation " + organisationId, metadata);
        return true;
    }

    private boolean isSuperAdmin(TenantAwareUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(Permission.PLATFORM_ADMIN.authority()));
    }
}
