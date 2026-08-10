package com.gymmate.user.application.port;

import java.util.UUID;

/**
 * Enforces an organisation's member-count plan limit before a new member is
 * created — see the port package Javadoc for why {@code MemberController} no longer
 * calls {@code organisation.application.OrganisationLimitService} directly (that was
 * the sole {@code user -> organisation} edge closing a gym/user/organisation module
 * cycle: organisation legitimately depends on gym, gym legitimately depends on user).
 */
public interface MemberLimitGuard {

    /**
     * @throws com.gymmate.shared.exception.DomainException if the organisation has
     *         reached its member limit
     */
    void checkCanAddMember(UUID organisationId);
}
