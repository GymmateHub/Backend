package com.gymmate.access.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.access.internal.domain.AccessPoint;

import java.util.List;
import java.util.UUID;

public interface AccessPointRepository extends DomainRepository<AccessPoint, UUID> {

  List<AccessPoint> findByGymId(UUID gymId);

  List<AccessPoint> findByOrganisationId(UUID organisationId);
}
