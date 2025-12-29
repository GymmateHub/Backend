package com.gymmate.classes.application;

import com.gymmate.classes.domain.ClassCategory;
import com.gymmate.classes.infrastructure.ClassCategoryJpaRepository;
import com.gymmate.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassCategoryService {

  private final ClassCategoryJpaRepository categoryRepository;

  public ClassCategory createCategory(ClassCategory category) {
    if (category.getGymId() == null) throw new DomainException("MISSING_GYM", "Gym id is required");
    if (categoryRepository.existsByGymIdAndName(category.getGymId(), category.getName())) {
      throw new DomainException("DUPLICATE_CATEGORY", "Category with this name already exists");
    }
    return categoryRepository.save(category);
  }

  public ClassCategory getCategory(UUID id) {
    return categoryRepository.findById(id).orElseThrow(() -> new DomainException("NOT_FOUND", "Category not found"));
  }

  public List<ClassCategory> listByGym(UUID gymId) {
    return categoryRepository.findByGymId(gymId);
  }

  public ClassCategory updateCategory(ClassCategory category) {
    ClassCategory existing = getCategory(category.getId());
    existing.updateDetails(category.getName(), category.getDescription(), category.getColor());
    return categoryRepository.save(existing);
  }

  public void deleteCategory(UUID id) {
    ClassCategory existing = getCategory(id);
    categoryRepository.delete(existing);
  }
}
