package com.gymmate.organisation.application;

import com.gymmate.user.application.port.MemberLimitGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implements user's {@link MemberLimitGuard} port by delegating to this module's own
 * {@link OrganisationLimitService} — see the port Javadoc for why
 * {@code MemberController} no longer calls {@code OrganisationLimitService} directly.
 */
@Component
@RequiredArgsConstructor
public class UserMemberLimitGuardAdapter implements MemberLimitGuard {

    private final OrganisationLimitService organisationLimitService;

    @Override
    public void checkCanAddMember(UUID organisationId) {
        organisationLimitService.checkCanAddMember(organisationId);
    }
}
