package com.gymmate.user.application.port;

import com.gymmate.shared.constants.MemberStatus;
import com.gymmate.shared.constants.UserRole;
import java.util.Optional;
import java.util.UUID;

/**
 * Port exposing read query capabilities of the {@code user} module to other modules,
 * replacing direct imports of {@code UserRepository} and {@code MemberRepository}.
 */
public interface UserQueryPort {

    Optional<UserOwnerSummary> findOwnerSummaryById(UUID ownerId);

    long countMembersByGymId(UUID gymId);

    long countActiveMembersByGymId(UUID gymId, MemberStatus status);

    long countUsersByOrganisationAndRole(UUID organisationId, UserRole role);

    record UserOwnerSummary(UUID id, UserRole role, boolean active, UUID organisationId) {}
}
