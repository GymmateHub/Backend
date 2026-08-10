package com.gymmate.whitelabel.internal.web;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.security.TenantAwareUserDetails;
import com.gymmate.whitelabel.api.dto.*;
import com.gymmate.whitelabel.application.DynamicMailSenderFactory;
import com.gymmate.whitelabel.application.WhitelabelSettingsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing Whitelabel branding, custom SMTP, WhatsApp credentials,
 * and Newsletter provider API keys for Organisations and Gyms.
 */
@Slf4j
@RestController
@RequestMapping("/api/whitelabel")
@RequiredArgsConstructor
@Tag(name = "Whitelabeling", description = "Whitelabeling, Custom SMTP, WhatsApp & Newsletter Settings APIs")
public class WhitelabelController {

    private final WhitelabelSettingsService whitelabelSettingsService;
    private final DynamicMailSenderFactory mailSenderFactory;

    /**
     * Get Organisation Whitelabel settings.
     */
    @GetMapping("/organisation")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GYM_OWNER', 'OWNER', 'ADMIN')")
    @Operation(summary = "Get Organisation Whitelabel settings")
    public ResponseEntity<ApiResponse<WhitelabelSettingsResponse>> getOrganisationSettings(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        UUID organisationId = requireOrganisationId(userDetails);
        WhitelabelSettingsResponse response = whitelabelSettingsService.getOrganisationSettingsResponse(organisationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update Organisation Whitelabel settings.
     */
    @PutMapping("/organisation")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GYM_OWNER', 'OWNER', 'ADMIN')")
    @Operation(summary = "Update Organisation Whitelabel settings (Branding, SMTP, WhatsApp, Newsletter)")
    public ResponseEntity<ApiResponse<WhitelabelSettingsResponse>> updateOrganisationSettings(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails,
            @Valid @RequestBody WhitelabelSettingsRequest request) {
        UUID organisationId = requireOrganisationId(userDetails);
        WhitelabelSettingsResponse response = whitelabelSettingsService.saveOrganisationSettings(organisationId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Organisation whitelabel settings saved successfully"));
    }

    /**
     * Get Gym Whitelabel settings.
     */
    @GetMapping("/gyms/{gymId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GYM_OWNER', 'OWNER', 'ADMIN', 'GYM_MANAGER', 'STAFF')")
    @Operation(summary = "Get Gym Whitelabel settings")
    public ResponseEntity<ApiResponse<WhitelabelSettingsResponse>> getGymSettings(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails,
            @PathVariable UUID gymId) {
        UUID organisationId = requireOrganisationId(userDetails);
        WhitelabelSettingsResponse response = whitelabelSettingsService.getGymSettingsResponse(organisationId, gymId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update Gym Whitelabel settings.
     */
    @PutMapping("/gyms/{gymId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GYM_OWNER', 'OWNER', 'ADMIN')")
    @Operation(summary = "Update Gym Whitelabel settings")
    public ResponseEntity<ApiResponse<WhitelabelSettingsResponse>> updateGymSettings(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails,
            @PathVariable UUID gymId,
            @Valid @RequestBody WhitelabelSettingsRequest request) {
        UUID organisationId = requireOrganisationId(userDetails);
        WhitelabelSettingsResponse response = whitelabelSettingsService.saveGymSettings(organisationId, gymId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Gym whitelabel settings saved successfully"));
    }

    /**
     * Test Custom SMTP credentials.
     */
    @PostMapping("/test-smtp")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GYM_OWNER', 'OWNER', 'ADMIN')")
    @Operation(summary = "Test custom SMTP configuration", description = "Validates custom SMTP settings by attempting to send a test email")
    public ResponseEntity<ApiResponse<TestConnectionResponse>> testSmtpConnection(
            @Valid @RequestBody SmtpTestRequest request) {
        try {
            mailSenderFactory.testSmtpConnection(
                    request.getSmtpHost(),
                    request.getSmtpPort(),
                    request.getSmtpUsername(),
                    request.getSmtpPassword(),
                    request.getSmtpSecurity(),
                    request.getFromEmail(),
                    request.getRecipientEmail());

            return ResponseEntity.ok(ApiResponse.success(
                    TestConnectionResponse.success("SMTP connection test succeeded! Test email sent to " + request.getRecipientEmail())));
        } catch (Exception e) {
            log.error("SMTP test failed: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.success(
                    TestConnectionResponse.failure("SMTP test failed", e.getMessage())));
        }
    }

    /**
     * Test WhatsApp credentials.
     */
    @PostMapping("/test-whatsapp")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GYM_OWNER', 'OWNER', 'ADMIN')")
    @Operation(summary = "Test WhatsApp credentials", description = "Validates WhatsApp Business API credentials by sending a test message")
    public ResponseEntity<ApiResponse<TestConnectionResponse>> testWhatsAppConnection(
            @Valid @RequestBody WhatsAppTestRequest request) {
        try {
            whitelabelSettingsService.sendWhatsAppMessage(
                    request.getWhatsappProvider(),
                    request.getWhatsappPhoneNumberId(),
                    request.getWhatsappApiKey(),
                    request.getRecipientPhoneNumber(),
                    "GymMate Whitelabel WhatsApp Test: Your credentials are valid!"
            );

            return ResponseEntity.ok(ApiResponse.success(
                    TestConnectionResponse.success("WhatsApp test message sent successfully to " + request.getRecipientPhoneNumber())));
        } catch (Exception e) {
            log.error("WhatsApp test failed: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.success(
                    TestConnectionResponse.failure("WhatsApp test failed", e.getMessage())));
        }
    }

    private UUID requireOrganisationId(TenantAwareUserDetails userDetails) {
        if (userDetails != null && userDetails.getOrganisationId() != null) {
            return userDetails.getOrganisationId();
        }
        UUID orgId = TenantContext.getCurrentTenantId();
        if (orgId == null) {
            throw new DomainException("NO_ORGANISATION_CONTEXT", "Organisation context required");
        }
        return orgId;
    }
}
