package com.gymmate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security configuration for the application.
 * Provides different security configurations for development and production environments.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final CorsConfigurationSource corsConfigurationSource;

  public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
    this.corsConfigurationSource = corsConfigurationSource;
  }

  @Bean
  @Profile("!prod")
  public SecurityFilterChain securityFilterChainDev(HttpSecurity http) throws Exception {
    http
      // Disable CSRF for development
      .csrf(AbstractHttpConfigurer::disable)

      // Enable CORS with the provided configuration
      .cors(cors -> cors.configurationSource(corsConfigurationSource))

      // Configure authorization
      .authorizeHttpRequests(auth -> auth
        // Public endpoints
        .requestMatchers(
          "/v3/api-docs/**",
          "/swagger-ui/**",
          "/swagger-ui.html",
          "/webjars/**",
          "/actuator/health",
          "/actuator/info"
        ).permitAll()
        // Allow all other requests in development
        .anyRequest().permitAll()
      );

    return http.build();
  }

  @Bean
  @Profile("prod")
  public SecurityFilterChain securityFilterChainProd(HttpSecurity http) throws Exception {
    http
      // Enable CSRF protection for production
      .csrf(csrf -> csrf.ignoringRequestMatchers(
        "/v3/api-docs/**",
        "/swagger-ui/**"
      ))

      // Enable CORS with the provided configuration
      .cors(cors -> cors.configurationSource(corsConfigurationSource))

      // Configure authorization
      .authorizeHttpRequests(auth -> auth
        // Public endpoints
        .requestMatchers(
          "/v3/api-docs/**",
          "/swagger-ui/**",
          "/swagger-ui.html",
          "/webjars/**",
          "/actuator/health",
          "/actuator/info"
        ).permitAll()
        // Require authentication for all other requests
        .anyRequest().authenticated()
      )

      // Enable form login for production
      .formLogin(form -> form
        .loginPage("/login")
        .permitAll()
      )

      // Enable HTTP Basic authentication
      .httpBasic(AbstractHttpConfigurer::disable);

    return http.build();
  }
}
