package com.gymmate.classes.application;

import com.gymmate.classes.domain.GymClass;
import com.gymmate.classes.infrastructure.GymClassJpaRepository;
import com.gymmate.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GymClassService {
  private final GymClassJpaRepository classRepository;

  public GymClass createClass(GymClass gymClass, UUID gymId) {
    if (gymClass.getCategoryId() == null) throw new DomainException("MISSING_CATEGORY", "Category id is required");
    if (classRepository.existsByGymIdAndName(gymId, gymClass.getName())) {
      throw new DomainException("DUPLICATE_CLASS", "Class with this name already exists");
    }
    return classRepository.save(gymClass);
  }

  public GymClass getClass(UUID id) {
    return classRepository.findById(id).orElseThrow(() -> new DomainException("NOT_FOUND", "Gym class not found"));
  }

  public List<GymClass> listByGym(UUID gymId) {
    return classRepository.findByGymId(gymId);
  }

  public GymClass updateClass(GymClass gymClass) {
    GymClass existing = getClass(gymClass.getId());
    existing.updateDetails(gymClass.getName(), gymClass.getDescription(), gymClass.getDurationMinutes());
    existing.updatePricing(gymClass.getPrice(), gymClass.getCreditsRequired());
    existing.updateCapacity(gymClass.getCapacity());
    return classRepository.save(existing);
  }

  public void deleteClass(UUID id) {
    GymClass existing = getClass(id);
    classRepository.delete(existing);
  }
}
