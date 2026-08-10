package com.gymmate.user.infrastructure;

import com.gymmate.shared.constants.MemberStatus;
import com.gymmate.shared.constants.UserRole;
import com.gymmate.user.application.port.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure adapter implementing {@link UserQueryPort} for inter-module read queries.
 */
@Component
@RequiredArgsConstructor
public class UserQueryAdapter implements UserQueryPort {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    @Override
    public Optional<UserOwnerSummary> findOwnerSummaryById(UUID ownerId) {
        return userRepository.findById(ownerId)
                .map(user -> new UserOwnerSummary(
                        user.getId(),
                        user.getRole(),
                        user.isActive(),
                        user.getOrganisationId()
                ));
    }

    @Override
    public long countMembersByGymId(UUID gymId) {
        return memberRepository.countByGymId(gymId);
    }

    @Override
    public long countActiveMembersByGymId(UUID gymId, MemberStatus status) {
        return memberRepository.countByGymIdAndStatus(gymId, status);
    }

    @Override
    public long countUsersByOrganisationAndRole(UUID organisationId, UserRole role) {
        return userRepository.countByOrganisationIdAndRole(organisationId, role);
    }
}
