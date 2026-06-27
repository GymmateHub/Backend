package com.gymmate.access.api;

import com.gymmate.access.api.dto.*;
import com.gymmate.access.application.AccessService;
import com.gymmate.access.application.IssuedCredential;
import com.gymmate.access.domain.AccessEvent;
import com.gymmate.access.domain.AccessPoint;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
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
import java.util.stream.Collectors;

/**
 * REST controller for access control & anti-tailgating.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access")
@RequiredArgsConstructor
@Tag(name = "Access Control", description = "Access control, check-in and anti-tailgating APIs")
public class AccessController {

  private final AccessService accessService;

  @PostMapping("/scan")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN', 'GYM_OWNER', 'MANAGER', 'STAFF')")
  @Operation(summary = "Scan credential", description = "Validate a credential at an access point and decide entry")
  public ResponseEntity<ApiResponse<ScanResponse>> scan(@Valid @RequestBody ScanRequest request) {
    AccessEvent event = accessService.scan(request.token(), request.accessPointId(), request.direction());
    ScanResponse body = ScanResponse.fromEntity(event);
    String message = body.granted() ? "Access granted" : "Access denied: " + body.denyReason();
    return ResponseEntity.ok(ApiResponse.success(body, message));
  }

  @PostMapping("/access-points")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN', 'GYM_OWNER', 'MANAGER')")
  @Operation(summary = "Create access point", description = "Register a controlled entry point")
  public ResponseEntity<ApiResponse<AccessPointResponse>> createAccessPoint(
      @Valid @RequestBody AccessPointCreateRequest request) {

    AccessPoint.AccessPointBuilder builder = AccessPoint.builder()
        .name(request.name())
        .areaId(request.areaId())
        .deviceId(request.deviceId());
    if (request.type() != null) builder.type(request.type());
    if (request.mode() != null) builder.mode(request.mode());
    if (request.reentryLockoutSeconds() != null) builder.reentryLockoutSeconds(request.reentryLockoutSeconds());

    AccessPoint point = builder.build();
    point.setOrganisationId(TenantContext.getCurrentTenantId());
    point.setGymId(request.gymId() != null ? request.gymId() : TenantContext.getCurrentGymId());

    AccessPoint created = accessService.createAccessPoint(point);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(AccessPointResponse.fromEntity(created), "Access point created"));
  }

  @GetMapping("/access-points/gym/{gymId}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN', 'GYM_OWNER', 'MANAGER', 'STAFF')")
  @Operation(summary = "List access points", description = "List access points for a gym")
  public ResponseEntity<ApiResponse<List<AccessPointResponse>>> getAccessPoints(@PathVariable UUID gymId) {
    List<AccessPointResponse> points = accessService.getAccessPointsByGym(gymId).stream()
        .map(AccessPointResponse::fromEntity).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(points));
  }

  @PostMapping("/credentials")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN', 'GYM_OWNER', 'MANAGER', 'STAFF')")
  @Operation(summary = "Issue credential", description = "Issue a QR/PIN/NFC credential for a member (token shown once)")
  public ResponseEntity<ApiResponse<CredentialIssuedResponse>> issueCredential(
      @Valid @RequestBody IssueCredentialRequest request) {
    IssuedCredential issued = accessService.issueCredential(request.memberId(), request.type(), request.expiresAt());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(CredentialIssuedResponse.from(issued), "Credential issued"));
  }

  @DeleteMapping("/credentials/{id}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN', 'GYM_OWNER', 'MANAGER', 'STAFF')")
  @Operation(summary = "Revoke credential", description = "Revoke a member access credential")
  public ResponseEntity<ApiResponse<Void>> revokeCredential(@PathVariable UUID id) {
    accessService.revokeCredential(id);
    return ResponseEntity.ok(ApiResponse.success(null, "Credential revoked"));
  }

  @GetMapping("/credentials/member/{memberId}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN', 'GYM_OWNER', 'MANAGER', 'STAFF')")
  @Operation(summary = "List member credentials", description = "List credentials issued to a member")
  public ResponseEntity<ApiResponse<List<AccessCredentialResponse>>> getMemberCredentials(@PathVariable UUID memberId) {
    List<AccessCredentialResponse> creds = accessService.getCredentialsByMember(memberId).stream()
        .map(AccessCredentialResponse::fromEntity).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(creds));
  }

  @GetMapping("/events/gym/{gymId}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN', 'GYM_OWNER', 'MANAGER', 'STAFF')")
  @Operation(summary = "List access events", description = "Audit log / tailgating report for a gym")
  public ResponseEntity<ApiResponse<List<AccessEventResponse>>> getEvents(
      @PathVariable UUID gymId,
      @RequestParam(name = "tailgating", required = false, defaultValue = "false") boolean tailgatingOnly) {
    List<AccessEventResponse> events = accessService.getEventsByGym(gymId, tailgatingOnly).stream()
        .map(AccessEventResponse::fromEntity).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(events));
  }
}
