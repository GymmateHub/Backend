package com.gymmate.classes.infrastructure;

import com.gymmate.classes.domain.GymArea;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GymAreaRepository {

  GymArea save(GymArea area);

  Optional<GymArea> findById(UUID id);

  List<GymArea> findByGymId(UUID gymId);

  Optional<GymArea> findByGymIdAndName(UUID gymId, String name);

  void delete(GymArea area);

  boolean existsByGymIdAndName(UUID gymId, String name);

}

