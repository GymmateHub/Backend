package com.gymmate.notification.application.port;

import com.gymmate.notification.domain.AudienceType;

import java.util.Set;
import java.util.UUID;

/**
 * Resolves the set of member IDs matching a filtered audience type
 * ({@code CLASS_SUBSCRIBERS}, {@code BOOKING_PARTICIPANTS}, {@code MEMBERSHIP_PLAN} —
 * not {@code ALL_MEMBERS}/{@code CUSTOM}, which {@link MemberDirectory} and
 * {@code AudienceResolver} handle directly without needing per-type data). Each
 * supported {@link AudienceType} should have exactly one implementation registered as
 * a Spring bean; {@code AudienceResolver} picks the first one that
 * {@link #supports(AudienceType)} the requested type.
 */
public interface AudienceMemberIdsResolver {

    boolean supports(AudienceType type);

    /**
     * @return the matching member IDs, or {@code null} to signal "no usable filter was
     *         provided (or resolution failed) — the caller should fall back to a
     *         broader/default audience" as the original single-module implementation
     *         did. An empty (non-null) set means "filter was valid, nothing matched" —
     *         those two cases are not the same and callers must not conflate them.
     */
    Set<UUID> resolveMemberIds(UUID gymId, AudienceType type, String audienceFilter);
}
