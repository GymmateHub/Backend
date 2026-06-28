package com.gymmate.access.infrastructure;

import com.gymmate.access.domain.AccessSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccessScheduleRepository extends JpaRepository<AccessSchedule, UUID> {

  List<AccessSchedule> findByMembershipPlanId(UUID membershipPlanId);
}
