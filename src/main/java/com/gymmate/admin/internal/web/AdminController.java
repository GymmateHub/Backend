package com.gymmate.admin.internal.web;

import com.gymmate.admin.api.dto.PlatformOverview;
import com.gymmate.admin.internal.service.AdminService;
import com.gymmate.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gymmate.admin.api.dto.TenantSummary;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Platform Administration APIs")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get platform overview", description = "Get high-level statistics for the platform")
    public ResponseEntity<ApiResponse<PlatformOverview>> getOverview() {
        PlatformOverview overview = adminService.getPlatformOverview();
        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    @GetMapping("/organisations")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all organisations", description = "Get a list of all organisations/tenants on the platform")
    public ResponseEntity<ApiResponse<List<TenantSummary>>> getOrganisations() {
        List<TenantSummary> organisations = adminService.getOrganisations();
        return ResponseEntity.ok(ApiResponse.success(organisations));
    }
}
