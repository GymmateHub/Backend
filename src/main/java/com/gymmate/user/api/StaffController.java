package com.gymmate.user.api;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.security.TenantAwareUserDetails;
import com.gymmate.user.api.dto.StaffCreateRequest;
import com.gymmate.user.api.dto.StaffResponse;
import com.gymmate.user.api.dto.StaffUpdateRequest;
import com.gymmate.user.application.StaffService;
import com.gymmate.user.domain.Staff;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for staff management operations.
 */
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Tag(name = "Staff", description = "Staff management operations")
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create staff", description = "Create a new staff profile")
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(@Valid @RequestBody StaffCreateRequest request) {
        Staff staff = staffService.createStaff(
                request.userId(),
                request.position(),
                request.department(),
                request.hourlyWage(),
                request.hireDate(),
                request.employmentType()
        );
        StaffResponse response = StaffResponse.fromEntity(staff);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Staff created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<StaffResponse>> getStaffById(@PathVariable UUID id) {
        Staff staff = staffService.findById(id);
        // Tenant isolation: findById bypasses Hibernate @Filter
        UUID tenantId = TenantContext.getCurrentTenantId();
        if (tenantId != null && staff.getOrganisationId() != null
                && !tenantId.equals(staff.getOrganisationId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You do not have permission to access this staff member"));
        }
        StaffResponse response = StaffResponse.fromEntity(staff);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<StaffResponse>> getStaffByUserId(@PathVariable UUID userId) {
        Staff staff = staffService.findByUserId(userId);
        // Tenant isolation: findByUserId may bypass Hibernate @Filter
        UUID tenantId = TenantContext.getCurrentTenantId();
        if (tenantId != null && staff.getOrganisationId() != null
                && !tenantId.equals(staff.getOrganisationId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You do not have permission to access this staff member"));
        }
        StaffResponse response = StaffResponse.fromEntity(staff);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get all staff", description = "Get all staff in the current organisation")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getAllStaff(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        UUID organisationId = userDetails.getOrganisationId();
        List<Staff> staff = staffService.findAllByOrganisation(organisationId);
        List<StaffResponse> responses = staff.stream()
                .map(StaffResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get active staff", description = "Get active staff in the current organisation")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getActiveStaff(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        UUID organisationId = userDetails.getOrganisationId();
        List<Staff> staff = staffService.findAllActive(organisationId);
        List<StaffResponse> responses = staff.stream()
                .map(StaffResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get staff by department", description = "Get staff by department in the current organisation")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getStaffByDepartment(
            @PathVariable String department,
            @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        UUID organisationId = userDetails.getOrganisationId();
        List<Staff> staff = staffService.findByDepartment(organisationId, department);
        List<StaffResponse> responses = staff.stream()
                .map(StaffResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}/position")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> updatePosition(
            @PathVariable UUID id,
            @Valid @RequestBody StaffUpdateRequest request) {
        Staff staff = staffService.updatePosition(id, request.position(), request.department());
        StaffResponse response = StaffResponse.fromEntity(staff);
        return ResponseEntity.ok(ApiResponse.success(response, "Position updated successfully"));
    }

    @PutMapping("/{id}/wage")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> updateWage(
            @PathVariable UUID id,
            @Valid @RequestBody StaffUpdateRequest request) {
        Staff staff = staffService.updateWage(id, request.hourlyWage());
        StaffResponse response = StaffResponse.fromEntity(staff);
        return ResponseEntity.ok(ApiResponse.success(response, "Wage updated successfully"));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> activate(@PathVariable UUID id) {
        Staff staff = staffService.activate(id);
        StaffResponse response = StaffResponse.fromEntity(staff);
        return ResponseEntity.ok(ApiResponse.success(response, "Staff activated successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> deactivate(@PathVariable UUID id) {
        Staff staff = staffService.deactivate(id);
        StaffResponse response = StaffResponse.fromEntity(staff);
        return ResponseEntity.ok(ApiResponse.success(response, "Staff deactivated successfully"));
    }
}
