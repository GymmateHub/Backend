package com.gymmate.configuration.internal.infrastructure.web;

import com.gymmate.configuration.internal.application.MessageTemplateConfigService;
import com.gymmate.configuration.internal.application.dto.MessageTemplateConfigResponse;
import com.gymmate.configuration.internal.application.dto.UpdateMessageTemplateRequest;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST endpoints for managing platform-wide default and organisation-specific
 * email and notification message templates.
 */
@RestController
@RequestMapping("/api/configurations/message-templates")
@RequiredArgsConstructor
@Tag(name = "Message Template Configuration", description = "Centralized management of email and notification message templates")
public class MessageTemplateConfigController {

    private final MessageTemplateConfigService service;

    @GetMapping("/platform")
    @PreAuthorize("hasAuthority('platform:admin')")
    @Operation(summary = "List platform default message templates", description = "Returns all platform-wide email and notification templates")
    public ResponseEntity<ApiResponse<List<MessageTemplateConfigResponse>>> listPlatformTemplates() {
        return ResponseEntity.ok(ApiResponse.success(service.listPlatformTemplates()));
    }

    @GetMapping("/platform/{templateKey}")
    @PreAuthorize("hasAuthority('platform:admin')")
    @Operation(summary = "Get platform default message template", description = "Returns a single platform-wide message template by key")
    public ResponseEntity<ApiResponse<MessageTemplateConfigResponse>> getPlatformTemplate(
            @PathVariable String templateKey) {
        return ResponseEntity.ok(ApiResponse.success(service.getPlatformTemplate(templateKey)));
    }

    @PutMapping("/platform/{templateKey}")
    @PreAuthorize("hasAuthority('platform:admin')")
    @Operation(summary = "Update platform default message template", description = "Updates a platform-wide email or notification message template")
    public ResponseEntity<ApiResponse<MessageTemplateConfigResponse>> updatePlatformTemplate(
            @PathVariable String templateKey,
            @Valid @RequestBody UpdateMessageTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                service.updatePlatformTemplate(templateKey, request),
                "Platform message template updated successfully"));
    }

    @PostMapping("/platform/{templateKey}/reset")
    @PreAuthorize("hasAuthority('platform:admin')")
    @Operation(summary = "Reset platform message template to factory default", description = "Restores the built-in default subject/title and body for a platform template")
    public ResponseEntity<ApiResponse<MessageTemplateConfigResponse>> resetPlatformTemplate(
            @PathVariable String templateKey) {
        return ResponseEntity.ok(ApiResponse.success(
                service.resetPlatformTemplate(templateKey),
                "Platform message template reset to default"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('org:manage')")
    @Operation(summary = "List effective organisation message templates", description = "Returns all message templates for the current organisation, including any tenant overrides")
    public ResponseEntity<ApiResponse<List<MessageTemplateConfigResponse>>> listOrganisationTemplates() {
        UUID organisationId = TenantContext.requireCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success(service.listOrganisationTemplates(organisationId)));
    }

    @GetMapping("/{templateKey}")
    @PreAuthorize("hasAuthority('org:manage')")
    @Operation(summary = "Get effective organisation message template", description = "Returns the effective message template for the current organisation")
    public ResponseEntity<ApiResponse<MessageTemplateConfigResponse>> getOrganisationTemplate(
            @PathVariable String templateKey) {
        UUID organisationId = TenantContext.requireCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success(service.getOrganisationTemplate(organisationId, templateKey)));
    }

    @PutMapping("/{templateKey}")
    @PreAuthorize("hasAuthority('org:manage')")
    @Operation(summary = "Create or update organisation message template override", description = "Customises an email or notification message template for the current organisation")
    public ResponseEntity<ApiResponse<MessageTemplateConfigResponse>> upsertOrganisationOverride(
            @PathVariable String templateKey,
            @Valid @RequestBody UpdateMessageTemplateRequest request) {
        UUID organisationId = TenantContext.requireCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success(
                service.upsertOrganisationOverride(organisationId, templateKey, request),
                "Organisation message template updated successfully"));
    }

    @DeleteMapping("/{templateKey}/override")
    @PreAuthorize("hasAuthority('org:manage')")
    @Operation(summary = "Reset organisation message template override", description = "Deletes the organisation override so the template reverts to the platform default")
    public ResponseEntity<ApiResponse<MessageTemplateConfigResponse>> deleteOrganisationOverride(
            @PathVariable String templateKey) {
        UUID organisationId = TenantContext.requireCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success(
                service.deleteOrganisationOverride(organisationId, templateKey),
                "Organisation override removed; reverted to platform default"));
    }
}

