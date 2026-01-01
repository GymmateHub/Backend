package com.gymmate.user.api;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.user.api.dto.StaffCreateRequest;
import com.gymmate.user.api.dto.StaffResponse;
import com.gymmate.user.api.dto.StaffUpdateRequest;
import com.gymmate.user.application.StaffService;
import com.gymmate.user.domain.Staff;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<StaffResponse>> getStaffById(@PathVariable UUID id) {
        Staff staff = staffService.findById(id);
        StaffResponse response = StaffResponse.fromEntity(staff);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<StaffResponse>> getStaffByUserId(@PathVariable UUID userId) {
        Staff staff = staffService.findByUserId(userId);
        StaffResponse response = StaffResponse.fromEntity(staff);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getAllStaff() {
        List<Staff> staff = staffService.findAll();
        List<StaffResponse> responses = staff.stream()
                .map(StaffResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getActiveStaff() {
        List<Staff> staff = staffService.findAllActive();
        List<StaffResponse> responses = staff.stream()
                .map(StaffResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getStaffByDepartment(@PathVariable String department) {
        List<Staff> staff = staffService.findByDepartment(department);
        List<StaffResponse> responses = staff.stream()
                .map(StaffResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}/position")
    public ResponseEntity<ApiResponse<StaffResponse>> updatePosition(
            @PathVariable UUID id,
            @Valid @RequestBody StaffUpdateRequest request) {
        Staff staff = staffService.updatePosition(id, request.position(), request.department());
        StaffResponse response = StaffResponse.fromEntity(staff);
        return ResponseEntity.ok(ApiResponse.success(response, "Position updated successfully"));
    }

    @PutMapping("/{id}/wage")
    public ResponseEntity<ApiResponse<StaffResponse>> updateWage(
            @PathVariable UUID id,
            @Valid @RequestBody StaffUpdateRequest request) {
        Staff staff = staffService.updateWage(id, request.hourlyWage());
        StaffResponse response = StaffResponse.fromEntity(staff);
        return ResponseEntity.ok(ApiResponse.success(response, "Wage updated successfully"));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<StaffResponse>> activate(@PathVariable UUID id) {
        Staff staff = staffService.activate(id);
        StaffResponse response = StaffResponse.fromEntity(staff);
        return ResponseEntity.ok(ApiResponse.success(response, "Staff activated successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<StaffResponse>> deactivate(@PathVariable UUID id) {
        Staff staff = staffService.deactivate(id);
        StaffResponse response = StaffResponse.fromEntity(staff);
        return ResponseEntity.ok(ApiResponse.success(response, "Staff deactivated successfully"));
    }
}
