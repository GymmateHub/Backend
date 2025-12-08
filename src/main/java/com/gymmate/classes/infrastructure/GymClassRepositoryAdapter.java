package com.gymmate.classes.infrastructure;

import com.gymmate.classes.domain.GymClass;
import com.gymmate.classes.domain.GymClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GymClassRepositoryAdapter implements GymClassRepository {
  private final GymClassJpaRepository jpaRepository;

  @Override
  public GymClass save(GymClass gymClass) {
    return jpaRepository.save(gymClass);
  }

  @Override
  public Optional<GymClass> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<GymClass> findByGymId(UUID gymId) {
    return jpaRepository.findByGymId(gymId);
  }

  @Override
  public List<GymClass> findByCategoryId(UUID categoryId) {
    return jpaRepository.findByCategoryId(categoryId);
  }

  @Override
  public List<GymClass> findActiveByGymId(UUID gymId) {
    return jpaRepository.findActiveByGymId(gymId);
  }

  @Override
  public Optional<GymClass> findByGymIdAndName(UUID gymId, String name) {
    return jpaRepository.findByGymIdAndName(gymId, name);
  }

  @Override
  public void delete(GymClass gymClass) {
    jpaRepository.delete(gymClass);
  }

  @Override
  public long countByGymId(UUID gymId) {
    return jpaRepository.countByGymId(gymId);
  }

  @Override
  public boolean existsByGymIdAndName(UUID gymId, String name) {
    return jpaRepository.existsByGymIdAndName(gymId, name);
  }
}

