package com.gymmate.notification.api;

import com.gymmate.notification.api.dto.*;
import com.gymmate.notification.application.NewsletterCampaignService;
import com.gymmate.notification.domain.NewsletterCampaign;
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
 * REST controller for newsletter campaign management.
 */
@Slf4j
@RestController
@RequestMapping("/api/newsletters/campaigns")
@RequiredArgsConstructor
@Tag(name = "Newsletter Campaigns", description = "Newsletter campaign management APIs")
public class NewsletterCampaignController {

    private final NewsletterCampaignService campaignService;
    private final JwtService jwtService;

    /**
     * Create a new campaign.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Create campaign", description = "Create a new newsletter campaign")
    public ResponseEntity<ApiResponse<CampaignResponse>> createCampaign(
            @Valid @RequestBody CreateCampaignRequest request,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);
        NewsletterCampaign campaign = campaignService.create(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        CampaignResponse.fromEntity(campaign),
                        "Campaign created successfully"));
    }

    /**
     * Get all campaigns for a gym.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "List campaigns", description = "Get all newsletter campaigns for a gym")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> getCampaigns(
            @RequestParam UUID gymId) {

        List<CampaignResponse> campaigns = campaignService.getByGymId(gymId)
                .stream()
                .map(CampaignResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    /**
     * Get a campaign by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get campaign", description = "Get a newsletter campaign by ID")
    public ResponseEntity<ApiResponse<CampaignResponse>> getCampaign(@PathVariable UUID id) {
        NewsletterCampaign campaign = campaignService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(CampaignResponse.fromEntity(campaign)));
    }

    /**
     * Preview campaign audience.
     */
    @GetMapping("/{id}/preview")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Preview audience", description = "Preview the target audience for a campaign")
    public ResponseEntity<ApiResponse<AudiencePreviewResponse>> previewAudience(@PathVariable UUID id) {
        AudiencePreviewResponse preview = campaignService.getAudiencePreview(id);
        return ResponseEntity.ok(ApiResponse.success(preview));
    }

    /**
     * Schedule a campaign for future delivery.
     */
    @PostMapping("/{id}/schedule")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Schedule campaign", description = "Schedule a campaign for future delivery")
    public ResponseEntity<ApiResponse<CampaignResponse>> scheduleCampaign(
            @PathVariable UUID id,
            @Valid @RequestBody ScheduleCampaignRequest request) {

        NewsletterCampaign campaign = campaignService.schedule(id, request.getScheduledAt());

        return ResponseEntity.ok(ApiResponse.success(
                CampaignResponse.fromEntity(campaign),
                "Campaign scheduled successfully"));
    }

    /**
     * Send a campaign immediately.
     */
    @PostMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Send campaign", description = "Send a campaign immediately to all recipients")
    public ResponseEntity<ApiResponse<CampaignResponse>> sendCampaign(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);
        NewsletterCampaign campaign = campaignService.send(id, userId);

        return ResponseEntity.ok(ApiResponse.success(
                CampaignResponse.fromEntity(campaign),
                "Campaign is being sent"));
    }

    /**
     * Cancel a scheduled campaign.
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Cancel campaign", description = "Cancel a scheduled campaign")
    public ResponseEntity<ApiResponse<CampaignResponse>> cancelCampaign(@PathVariable UUID id) {
        NewsletterCampaign campaign = campaignService.cancel(id);

        return ResponseEntity.ok(ApiResponse.success(
                CampaignResponse.fromEntity(campaign),
                "Campaign cancelled successfully"));
    }

    /**
     * Delete a campaign.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Delete campaign", description = "Delete a newsletter campaign")
    public ResponseEntity<ApiResponse<Void>> deleteCampaign(@PathVariable UUID id) {
        campaignService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Campaign deleted successfully"));
    }

    private UUID extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}
