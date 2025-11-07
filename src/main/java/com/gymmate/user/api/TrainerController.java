package com.gymmate.user.api;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.user.api.dto.TrainerCreateRequest;
import com.gymmate.user.api.dto.TrainerResponse;
import com.gymmate.user.api.dto.TrainerUpdateRequest;
import com.gymmate.user.application.TrainerService;
import com.gymmate.user.domain.Trainer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for trainer management operations.
 */
@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    @PostMapping
    public ResponseEntity<ApiResponse<TrainerResponse>> createTrainer(@Valid @RequestBody TrainerCreateRequest request) {
        Trainer trainer = trainerService.createTrainer(
                request.getUserId(),
                request.getSpecializations(),
                request.getBio(),
                request.getHourlyRate(),
                request.getCommissionRate(),
                request.getHireDate(),
                request.getEmploymentType()
        );
        TrainerResponse response = TrainerResponse.fromEntity(trainer);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Trainer created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrainerResponse>> getTrainerById(@PathVariable UUID id) {
        Trainer trainer = trainerService.findById(id);
        TrainerResponse response = TrainerResponse.fromEntity(trainer);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<TrainerResponse>> getTrainerByUserId(@PathVariable UUID userId) {
        Trainer trainer = trainerService.findByUserId(userId);
        TrainerResponse response = TrainerResponse.fromEntity(trainer);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TrainerResponse>>> getAllTrainers() {
        List<Trainer> trainers = trainerService.findAll();
        List<TrainerResponse> responses = trainers.stream()
                .map(TrainerResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<TrainerResponse>>> getAvailableTrainers() {
        List<Trainer> trainers = trainerService.findActiveAndAcceptingClients();
        List<TrainerResponse> responses = trainers.stream()
                .map(TrainerResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}/rate")
    public ResponseEntity<ApiResponse<TrainerResponse>> updateRate(
            @PathVariable UUID id,
            @Valid @RequestBody TrainerUpdateRequest request) {
        Trainer trainer = trainerService.updateRate(id, request.getHourlyRate(), request.getCommissionRate());
        TrainerResponse response = TrainerResponse.fromEntity(trainer);
        return ResponseEntity.ok(ApiResponse.success(response, "Rate updated successfully"));
    }

    @PatchMapping("/{id}/toggle-accepting")
    public ResponseEntity<ApiResponse<TrainerResponse>> toggleAcceptingClients(@PathVariable UUID id) {
        Trainer trainer = trainerService.toggleAcceptingClients(id);
        TrainerResponse response = TrainerResponse.fromEntity(trainer);
        return ResponseEntity.ok(ApiResponse.success(response, "Accepting clients status toggled"));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<TrainerResponse>> activate(@PathVariable UUID id) {
        Trainer trainer = trainerService.activate(id);
        TrainerResponse response = TrainerResponse.fromEntity(trainer);
        return ResponseEntity.ok(ApiResponse.success(response, "Trainer activated successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<TrainerResponse>> deactivate(@PathVariable UUID id) {
        Trainer trainer = trainerService.deactivate(id);
        TrainerResponse response = TrainerResponse.fromEntity(trainer);
        return ResponseEntity.ok(ApiResponse.success(response, "Trainer deactivated successfully"));
    }
}

