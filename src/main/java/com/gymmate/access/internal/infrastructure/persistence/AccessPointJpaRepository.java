package com.gymmate.access.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccessPointJpaRepository extends JpaRepository<AccessPointJpaEntity, UUID> {

    List<AccessPointJpaEntity> findByGymId(UUID gymId);

    List<AccessPointJpaEntity> findByOrganisationId(UUID organisationId);
}
