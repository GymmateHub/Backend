package com.gymmate.classes.api;

import com.gymmate.classes.api.dto.AreaResponse;
import com.gymmate.classes.api.dto.CreateAreaRequest;
import com.gymmate.classes.api.dto.GymAreaMapper;
import com.gymmate.classes.application.GymAreaService;
import com.gymmate.classes.domain.GymArea;
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
@RequestMapping("/api/gym-areas")
@RequiredArgsConstructor
public class GymAreaController {
  private final GymAreaService areaService;
  private final GymAreaMapper mapper;

  @PostMapping
  @PreAuthorize("hasRole('GYM_OWNER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<AreaResponse>> create(@Valid @RequestBody CreateAreaRequest req) {
    GymArea a = mapper.toEntity(req);
    a.setGymId(req.getGymId());
    GymArea created = areaService.createArea(a);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(mapper.toResponse(created), "Area created"));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<AreaResponse>> get(@PathVariable UUID id) {
    GymArea a = areaService.getArea(id);
    return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(a)));
  }

  @GetMapping("/gym/{gymId}")
  public ResponseEntity<ApiResponse<List<AreaResponse>>> listByGym(@PathVariable UUID gymId) {
    List<GymArea> list = areaService.listByGym(gymId);
    List<AreaResponse> res = list.stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(res));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('GYM_OWNER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<AreaResponse>> update(@PathVariable UUID id, @Valid @RequestBody CreateAreaRequest req) {
    GymArea a = areaService.getArea(id);
    a.updateDetails(req.getName(), req.getAreaType(), req.getCapacity());
    GymArea updated = areaService.updateArea(a);
    return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(updated), "Area updated"));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('GYM_OWNER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
    areaService.deleteArea(id);
    return ResponseEntity.ok(ApiResponse.success(null, "Area deleted"));
  }
}
