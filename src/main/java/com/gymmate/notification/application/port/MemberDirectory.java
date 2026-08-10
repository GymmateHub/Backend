package com.gymmate.notification.application.port;

import com.gymmate.notification.application.AudienceResolver;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Looks up member/user data enriched into {@link AudienceResolver.MemberRecipient} —
 * the one implementation (in {@code user.infrastructure}) owns both {@code Member} and
 * {@code User}, which is exactly why this used to live inside {@code AudienceResolver}
 * as a direct repository dependency. See the port package Javadoc for why it moved.
 */
public interface MemberDirectory {

    List<AudienceResolver.MemberRecipient> findActiveMembersByGym(UUID gymId);

    /**
     * @param activeOnly whether to filter to {@code MemberStatus.ACTIVE} members —
     *                    audience types differ on this (e.g. class-subscriber
     *                    audiences filter active-only, booking-participant audiences
     *                    historically did not), so the caller decides, not this port.
     */
    List<AudienceResolver.MemberRecipient> findMembersByIds(UUID gymId, Set<UUID> memberIds, boolean activeOnly);
}
