package com.gymmate.classes.infrastructure;

import com.gymmate.classes.domain.ClassCategory;
import com.gymmate.classes.domain.ClassCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClassCategoryRepositoryAdapter implements ClassCategoryRepository {
  private final ClassCategoryJpaRepository jpaRepository;

  @Override
  public ClassCategory save(ClassCategory category) {
    return jpaRepository.save(category);
  }

  @Override
  public Optional<ClassCategory> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<ClassCategory> findByGymId(UUID gymId) {
    return jpaRepository.findByGymId(gymId);
  }

  @Override
  public List<ClassCategory> findActiveByGymId(UUID gymId) {
    return jpaRepository.findActiveByGymId(gymId);
  }

  @Override
  public Optional<ClassCategory> findByGymIdAndName(UUID gymId, String name) {
    return jpaRepository.findByGymIdAndName(gymId, name);
  }

  @Override
  public void delete(ClassCategory category) {
    jpaRepository.delete(category);
  }

  @Override
  public boolean existsByGymIdAndName(UUID gymId, String name) {
    return jpaRepository.existsByGymIdAndName(gymId, name);
  }
}

