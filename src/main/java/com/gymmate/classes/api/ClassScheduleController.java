package com.gymmate.classes.api;

import com.gymmate.classes.api.dto.ClassScheduleMapper;
import com.gymmate.classes.api.dto.CreateScheduleRequest;
import com.gymmate.classes.api.dto.ScheduleResponse;
import com.gymmate.classes.application.ClassScheduleService;
import com.gymmate.classes.domain.ClassSchedule;
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
@RequestMapping("/api/class-schedules")
@RequiredArgsConstructor
public class ClassScheduleController {
  private final ClassScheduleService scheduleService;
  private final ClassScheduleMapper mapper;

  @PostMapping
  @PreAuthorize("hasRole('GYM_OWNER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<ScheduleResponse>> create(@Valid @RequestBody CreateScheduleRequest req) {
    ClassSchedule s = mapper.toEntity(req);
    ClassSchedule created = scheduleService.createSchedule(s);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(mapper.toResponse(created), "Schedule created"));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ScheduleResponse>> get(@PathVariable UUID id) {
    ClassSchedule s = scheduleService.getSchedule(id);
    return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(s)));
  }

  @GetMapping("/gym/{gymId}")
  public ResponseEntity<ApiResponse<List<ScheduleResponse>>> listByGym(@PathVariable UUID gymId) {
    List<ClassSchedule> list = scheduleService.listByGym(gymId);
    List<ScheduleResponse> res = list.stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(res));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('GYM_OWNER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<ScheduleResponse>> update(@PathVariable UUID id, @Valid @RequestBody CreateScheduleRequest req) {
    ClassSchedule s = scheduleService.getSchedule(id);
    s.setStartTime(req.getStartTime());
    s.setEndTime(req.getEndTime());
    s.setCapacityOverride(req.getCapacityOverride());
    s.setPriceOverride(req.getPriceOverride());
    s.setTrainerId(req.getTrainerId());
    s.setAreaId(req.getAreaId());
    ClassSchedule updated = scheduleService.updateSchedule(s);
    return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(updated), "Schedule updated"));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('GYM_OWNER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
    scheduleService.deleteSchedule(id);
    return ResponseEntity.ok(ApiResponse.success(null, "Schedule deleted"));
  }
}
