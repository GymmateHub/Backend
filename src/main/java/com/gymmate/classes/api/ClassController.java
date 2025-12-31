package com.gymmate.classes.api;

import com.gymmate.classes.api.dto.ClassResponse;
import com.gymmate.classes.api.dto.CreateClassRequest;
import com.gymmate.classes.api.dto.GymClassMapper;
import com.gymmate.classes.application.GymClassService;
import com.gymmate.classes.domain.GymClass;
import com.gymmate.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {
  private final GymClassService classService;
  private final GymClassMapper mapper;

  @PostMapping
  @PreAuthorize("hasRole('GYM_OWNER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<ClassResponse>> createClass(@Valid @RequestBody CreateClassRequest req) {
    GymClass gc = mapper.toEntity(req);
    gc.setGymId(req.getGymId());
    GymClass created = classService.createClass(gc);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(mapper.toResponse(created), "Class created"));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ClassResponse>> getClass(@PathVariable UUID id) {
    GymClass gc = classService.getClass(id);
    return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(gc)));
  }

  @GetMapping("/gym/{gymId}")
  public ResponseEntity<ApiResponse<List<ClassResponse>>> listByGym(@PathVariable UUID gymId) {
    List<GymClass> list = classService.listByGym(gymId);
    List<ClassResponse> res = list.stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(res));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('GYM_OWNER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<ClassResponse>> updateClass(@PathVariable UUID id, @Valid @RequestBody CreateClassRequest req) {
    GymClass gc = classService.getClass(id);
    gc.updateDetails(req.getName(), req.getDescription(), req.getDurationMinutes());
    gc.updatePricing(req.getPrice(), req.getCreditsRequired());
    gc.updateCapacity(req.getCapacity());
    GymClass updated = classService.updateClass(gc);
    return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(updated), "Class updated"));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('GYM_OWNER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<Void>> deleteClass(@PathVariable UUID id) {
    classService.deleteClass(id);
    return ResponseEntity.ok(ApiResponse.success(null, "Class deleted"));
  }
}
