package com.gymmate.notification.api;

import com.gymmate.notification.api.dto.CreateTemplateRequest;
import com.gymmate.notification.api.dto.TemplateResponse;
import com.gymmate.notification.api.dto.UpdateTemplateRequest;
import com.gymmate.notification.application.NewsletterTemplateService;
import com.gymmate.notification.domain.NewsletterTemplate;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for newsletter template management.
 */
@Slf4j
@RestController
@RequestMapping("/api/newsletters/templates")
@RequiredArgsConstructor
@Tag(name = "Newsletter Templates", description = "Newsletter template management APIs")
public class NewsletterTemplateController {

    private final NewsletterTemplateService templateService;
    private final JwtService jwtService;

    /**
     * Create a new newsletter template.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Create template", description = "Create a new newsletter template")
    public ResponseEntity<ApiResponse<TemplateResponse>> createTemplate(
            @Valid @RequestBody CreateTemplateRequest request,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);
        NewsletterTemplate template = templateService.create(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        TemplateResponse.fromEntity(template),
                        "Template created successfully"));
    }

    /**
     * Get all templates for a gym.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "List templates", description = "Get all newsletter templates for a gym")
    public ResponseEntity<ApiResponse<List<TemplateResponse>>> getTemplates(
            @RequestParam UUID gymId) {

        List<TemplateResponse> templates = templateService.getActiveByGymId(gymId)
                .stream()
                .map(TemplateResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    /**
     * Get a template by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get template", description = "Get a newsletter template by ID")
    public ResponseEntity<ApiResponse<TemplateResponse>> getTemplate(@PathVariable UUID id) {
        NewsletterTemplate template = templateService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(TemplateResponse.fromEntity(template)));
    }

    /**
     * Update a template.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Update template", description = "Update a newsletter template")
    public ResponseEntity<ApiResponse<TemplateResponse>> updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTemplateRequest request) {

        NewsletterTemplate template = templateService.update(id, request);

        return ResponseEntity.ok(ApiResponse.success(
                TemplateResponse.fromEntity(template),
                "Template updated successfully"));
    }

    /**
     * Delete a template.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Delete template", description = "Delete a newsletter template")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID id) {
        templateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Template deleted successfully"));
    }

    private UUID extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}
