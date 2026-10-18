package com.gymmate.crm.internal.infrastructure.persistence;

import com.gymmate.crm.internal.domain.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeadJpaRepository extends JpaRepository<LeadJpaEntity, UUID> {

    List<LeadJpaEntity> findByGymId(UUID gymId);

    List<LeadJpaEntity> findByOrganisationId(UUID organisationId);

    List<LeadJpaEntity> findByGymIdAndStatus(UUID gymId, LeadStatus status);

    List<LeadJpaEntity> findByOrganisationIdAndStatus(UUID organisationId, LeadStatus status);

    long countByOrganisationIdAndStatus(UUID organisationId, LeadStatus status);
}
