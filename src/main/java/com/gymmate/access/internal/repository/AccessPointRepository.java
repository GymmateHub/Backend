package com.gymmate.access.internal.repository;

import com.gymmate.access.internal.domain.AccessPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccessPointRepository extends JpaRepository<AccessPoint, UUID> {

  List<AccessPoint> findByGymId(UUID gymId);

  List<AccessPoint> findByOrganisationId(UUID organisationId);
}
