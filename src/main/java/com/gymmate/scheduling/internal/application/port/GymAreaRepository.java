package com.gymmate.scheduling.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.scheduling.internal.domain.GymArea;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GymAreaRepository extends DomainRepository<GymArea, UUID> {

  List<GymArea> findByGymId(UUID gymId);

  Optional<GymArea> findByGymIdAndName(UUID gymId, String name);

  boolean existsByGymIdAndName(UUID gymId, String name);
}

