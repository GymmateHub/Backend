package com.gymmate.classes.api;

import com.gymmate.classes.api.dto.CategoryResponse;
import com.gymmate.classes.api.dto.CreateCategoryRequest;
import com.gymmate.classes.api.dto.ClassCategoryMapper;
import com.gymmate.classes.application.ClassCategoryService;
import com.gymmate.classes.domain.ClassCategory;
import com.gymmate.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/class-categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Category management operations")
public class ClassCategoryController {
  private final ClassCategoryService categoryService;
  private final ClassCategoryMapper mapper;

  @PostMapping
  @PreAuthorize("hasRole('GYM_OWNER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CreateCategoryRequest req) {
    ClassCategory c = mapper.toEntity(req);
    c.setGymId(req.getGymId());
    ClassCategory created = categoryService.createCategory(c);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(mapper.toResponse(created), "Category created"));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CategoryResponse>> get(@PathVariable UUID id) {
    ClassCategory c = categoryService.getCategory(id);
    return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(c)));
  }

  @GetMapping("/gym/{gymId}")
  public ResponseEntity<ApiResponse<List<CategoryResponse>>> listByGym(@PathVariable UUID gymId) {
    List<ClassCategory> list = categoryService.listByGym(gymId);
    List<CategoryResponse> res = list.stream().map(mapper::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(res));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('GYM_OWNER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<CategoryResponse>> update(@PathVariable UUID id, @Valid @RequestBody CreateCategoryRequest req) {
    ClassCategory c = categoryService.getCategory(id);
    c.updateDetails(req.getName(), req.getDescription(), req.getColor());
    ClassCategory updated = categoryService.updateCategory(c);
    return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(updated), "Category updated"));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('GYM_OWNER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
    categoryService.deleteCategory(id);
    return ResponseEntity.ok(ApiResponse.success(null, "Category deleted"));
  }
}
