package com.gymmate.access.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.access.internal.domain.AccessSchedule;

import java.util.List;
import java.util.UUID;

public interface AccessScheduleRepository extends DomainRepository<AccessSchedule, UUID> {

  List<AccessSchedule> findByMembershipPlanId(UUID membershipPlanId);
}
