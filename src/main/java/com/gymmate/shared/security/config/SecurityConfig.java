package com.gymmate.shared.security.config;

import com.gymmate.shared.security.CustomUserDetailsService;
import com.gymmate.shared.security.filter.JwtAuthenticationFilter;
import com.gymmate.shared.multitenancy.TenantFilter;
import com.gymmate.shared.security.filter.SecurityHeadersFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security configuration for the application.
 * Implements JWT-based authentication with Role-Based Access Control (RBAC).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantFilter tenantFilter;
    private final SecurityHeadersFilter securityHeadersFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(securityHeadersFilter, ChannelProcessingFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized: " +
                                    (authException.getMessage() != null ? authException.getMessage().replace("\"", "\\\"") : "Authentication required") +
                                    "\",\"status\":401}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // Deny sensitive paths
                        .requestMatchers("/.git/**", "/.env", "/config/**").denyAll()
                        // Public endpoints
                        .requestMatchers(
                                "/", "/error",
                                "/api/auth/login",
                                "/api/auth/register/**",
                                "/api/auth/invite/**",
                                "/api/auth/password-reset/**",
                                "/api/auth/refresh",
                                "/api/auth/email-status/**",
                                "/api/gyms/register", "/api/gyms/city/**",
                                "/api/users/register/gym-owner",
                                "/api/webhooks/**",
                                "/v3/api-docs/**", "/scalar.html",
                                "/actuator/**", "/actuator/info")
                        .permitAll()
                        // Role-based endpoints
                        .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/gyms/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN", "GYM_OWNER", "OWNER", "MANAGER")
                        .requestMatchers("/api/classes/**")
                        .hasAnyRole("TRAINER", "ADMIN", "SUPER_ADMIN", "GYM_OWNER", "OWNER", "MANAGER", "MEMBER")
                        .requestMatchers("/api/class-categories/**")
                        .hasAnyRole("TRAINER", "ADMIN", "SUPER_ADMIN", "GYM_OWNER", "OWNER", "MANAGER")
                        .requestMatchers("/api/class-schedules/**")
                        .hasAnyRole("TRAINER", "ADMIN", "SUPER_ADMIN", "GYM_OWNER", "OWNER", "MANAGER", "MEMBER")
                        .requestMatchers("/api/bookings/**")
                        .authenticated()
                        .requestMatchers("/api/staff/**")
                        .hasAnyRole("STAFF", "ADMIN", "SUPER_ADMIN", "GYM_OWNER", "OWNER", "MANAGER")
                        .requestMatchers("/api/inventory/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN", "GYM_OWNER", "OWNER", "MANAGER", "STAFF")
                        // All other endpoints require authentication
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(tenantFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * BUG-031: registerOwner() assigns UserRole.GYM_OWNER, but the vast majority of
     * 
     * @PreAuthorize checks across the codebase only allow 'OWNER'. Without this
     *               hierarchy,
     *               a freshly-registered owner gets 403 on almost every
     *               owner-scoped write endpoint
     *               (invite staff, create member, create second gym, switch gym)
     *               despite the URL-level
     *               matchers above already granting GYM_OWNER. Method security must
     *               know GYM_OWNER
     *               implies OWNER too.
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("GYM_OWNER").implies("OWNER")
                .build();
    }

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }

    /**
     * Prevent auto-registration of security filters as servlet filters.
     * These filters are managed exclusively by Spring Security's FilterChainProxy.
     * Without this, @Component + OncePerRequestFilter causes double-registration:
     * the filter runs as a servlet filter first, marks itself as "already
     * executed",
     * and then gets skipped inside the security chain — breaking authentication.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<TenantFilter> tenantFilterRegistration(TenantFilter filter) {
        FilterRegistrationBean<TenantFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilterRegistration(
            SecurityHeadersFilter filter) {
        FilterRegistrationBean<SecurityHeadersFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
