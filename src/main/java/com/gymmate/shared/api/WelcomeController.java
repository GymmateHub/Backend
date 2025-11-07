package com.gymmate.shared.api;

import com.gymmate.shared.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Welcome controller for the root endpoint.
 */
@RestController
public class WelcomeController {

    @Value("${spring.application.name:GymMate Backend}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> welcome() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", applicationName);
        info.put("status", "running");
        info.put("timestamp", LocalDateTime.now());
        info.put("message", "Welcome to GymMate API");
        info.put("docs", "/swagger-ui.html");
        info.put("health", "/actuator/health");

        return ResponseEntity.ok(ApiResponse.success(info, "API is running successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(ApiResponse.success(health, "Service is healthy"));
    }
}

