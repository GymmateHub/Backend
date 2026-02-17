package com.gymmate.shared.config;

import com.gymmate.shared.security.CustomUserDetailsService;
import com.gymmate.shared.security.JwtAuthenticationFilter;
import com.gymmate.shared.multitenancy.TenantFilter;
import lombok.RequiredArgsConstructor;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security configuration for the application.
 * Implements JWT-based authentication with Role-Based Access Control (RBAC).
 *
 * Key Features:
 * - Stateless session management using JWT tokens
 * - Role-based authorization with method-level security
 * - Custom UserDetailsService for user authentication
 * - CORS configuration for cross-origin requests
 * - Multi-tenancy support with tenant filter
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

    /**
     * Configure the security filter chain.
     * This is the main security configuration for HTTP requests.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection (not needed for stateless JWT authentication)
            .csrf(AbstractHttpConfigurer::disable)

            // Enable CORS with the provided configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // Set session management to stateless (no HTTP sessions)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Explicitly deny access to sensitive paths
                .requestMatchers("/.git/**", "/.env", "/config/**").denyAll()

                // Public endpoints - no authentication required
                .requestMatchers(
                    "/",
                    "/error",
                    "/api/auth/**",  // All auth endpoints including registration and invite
                    "/api/gyms/register",
                    "/api/gyms",  // Public listing of all gyms
                    "/api/gyms/active",  // Public listing of active gyms
                    "/api/gyms/city/**",  // Public search by city
                    "/api/gyms/slug/**",  // Public gym lookup by slug
                    "/api/webhooks/**",  // Stripe webhooks (signature verified internally)
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/webjars/**",
                    "/actuator/**",
                    "/actuator/info"
                ).permitAll()

                // Super Admin only endpoints
                .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")

                // Admin and Super Admin endpoints (for gym management)
                .requestMatchers("/api/gyms/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "OWNER")

                // Trainer endpoints
                .requestMatchers("/api/classes/**").hasAnyRole("TRAINER", "ADMIN", "SUPER_ADMIN")

                // Staff endpoints
                .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "ADMIN", "SUPER_ADMIN")

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

            // Set the authentication provider
            .authenticationProvider(authenticationProvider())

            // Add JWT authentication filter before the default authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // Add tenant filter after JWT authentication
            .addFilterAfter(tenantFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configure the authentication provider.
     * Uses DaoAuthenticationProvider with our custom UserDetailsService and PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }

    /**
     * Configure the authentication manager.
     * Required for authentication operations.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configure the password encoder.
     * Using BCrypt with default strength (10 rounds).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
