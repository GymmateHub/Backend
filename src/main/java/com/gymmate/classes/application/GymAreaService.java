package com.gymmate.classes.application;

import com.gymmate.classes.domain.GymArea;
import com.gymmate.classes.infrastructure.GymAreaJpaRepository;
import com.gymmate.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GymAreaService {
  private final GymAreaJpaRepository areaRepository;

  public GymArea createArea(GymArea area) {
    if (area.getGymId() == null) throw new DomainException("MISSING_GYM", "Gym id is required");
    if (areaRepository.existsByGymIdAndName(area.getGymId(), area.getName())) {
      throw new DomainException("DUPLICATE_AREA", "Area with this name already exists");
    }
    return areaRepository.save(area);
  }

  public GymArea getArea(UUID id) {
    return areaRepository.findById(id).orElseThrow(() -> new DomainException("NOT_FOUND", "Gym area not found"));
  }

  public List<GymArea> listByGym(UUID gymId) {
    return areaRepository.findByGymId(gymId);
  }

  public GymArea updateArea(GymArea area) {
    GymArea existing = getArea(area.getId());
    existing.updateDetails(area.getName(), area.getAreaType(), area.getCapacity());
    return areaRepository.save(existing);
  }

  public void deleteArea(UUID id) {
    GymArea existing = getArea(id);
    areaRepository.delete(existing);
  }
}
