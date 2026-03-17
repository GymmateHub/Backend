package com.gymmate.shared.security.config;

import com.gymmate.shared.security.CustomUserDetailsService;
import com.gymmate.shared.security.filter.JwtAuthenticationFilter;
import com.gymmate.shared.multitenancy.TenantFilter;
import com.gymmate.shared.security.filter.SecurityHeadersFilter;
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
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**",
                                "/actuator/**", "/actuator/info")
                        .permitAll()
                        // Role-based endpoints
                        .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/gyms/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "GYM_OWNER", "OWNER", "MANAGER")
                        .requestMatchers("/api/classes/**").hasAnyRole("TRAINER", "ADMIN", "SUPER_ADMIN", "GYM_OWNER", "OWNER", "MANAGER")
                        .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "ADMIN", "SUPER_ADMIN", "GYM_OWNER", "OWNER", "MANAGER")
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
     * Prevent auto-registration of security filters as servlet filters.
     * These filters are managed exclusively by Spring Security's FilterChainProxy.
     * Without this, @Component + OncePerRequestFilter causes double-registration:
     * the filter runs as a servlet filter first, marks itself as "already executed",
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
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilterRegistration(SecurityHeadersFilter filter) {
        FilterRegistrationBean<SecurityHeadersFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
