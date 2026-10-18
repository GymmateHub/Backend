package com.gymmate.identity.internal.infrastructure.security;

import com.gymmate.identity.internal.infrastructure.security.CustomUserDetailsService;
import com.gymmate.identity.internal.infrastructure.security.JwtAuthenticationFilter;
import com.gymmate.shared.multitenancy.TenantFilter;
import com.gymmate.shared.infrastructure.web.SecurityHeadersFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.web.session.DisableEncodeUrlFilter;
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
                // First filter in the chain (Spring Security 7 removed ChannelProcessingFilter),
                // so security headers are set on every response, including rejected requests.
                .addFilterBefore(securityHeadersFilter, DisableEncodeUrlFilter.class)
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
                                "/api/gyms/city/**",
                                "/api/users/register/gym-owner",
                                "/api/webhooks/**",
                                "/v3/api-docs/**", "/scalar.html",
                                "/actuator/**", "/actuator/info")
                        .permitAll()
                        // Endpoints authorise on permissions (@PreAuthorize("hasAuthority(...)"),
                        // granted per role in shared.security.RolePermissions). URL level only
                        // separates public from authenticated, plus the platform admin area.
                        .requestMatchers("/api/admin/**").hasAuthority("platform:admin")
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(tenantFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
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
