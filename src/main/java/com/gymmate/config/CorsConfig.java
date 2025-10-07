package com.gymmate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Allow all origins, methods, and headers in development
    configuration.setAllowedOrigins(List.of("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));

    // Expose specific headers to the client
    configuration.setExposedHeaders(Arrays.asList(
      "Authorization",
      "Content-Type",
      "Content-Disposition",
      "X-Requested-With",
      "Accept"
    ));

    // Allow credentials (cookies, HTTP authentication)
    configuration.setAllowCredentials(true);

    // Set max age of the CORS preflight request
    configuration.setMaxAge(3600L);

    // Configure CORS for all endpoints
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
