package com.gymmate.notification.application;

import com.gymmate.notification.api.dto.AudiencePreviewResponse;
import com.gymmate.notification.domain.AudienceType;
import com.gymmate.user.domain.Member;
import com.gymmate.user.domain.MemberStatus;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.MemberRepository;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for resolving target audience based on audience type and filters.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AudienceResolver {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    // TODO: Add ClassBookingRepository when integrating with bookings
    // TODO: Add MemberMembershipRepository when filtering by plan

    /**
     * DTO to hold combined member and user info for newsletters.
     */
    public record MemberRecipient(
            UUID memberId,
            UUID userId,
            String firstName,
            String lastName,
            String email) {
    }

    /**
     * Resolve the target members based on audience type and filter.
     */
    @Transactional(readOnly = true)
    public List<MemberRecipient> resolveAudience(UUID gymId, AudienceType audienceType, String audienceFilter) {
        log.debug("Resolving audience for gym: {}, type: {}", gymId, audienceType);

        List<Member> members = switch (audienceType) {
            case ALL_MEMBERS -> resolveAllMembers(gymId);
            case CLASS_SUBSCRIBERS -> resolveClassSubscribers(gymId, audienceFilter);
            case BOOKING_PARTICIPANTS -> resolveBookingParticipants(gymId, audienceFilter);
            case MEMBERSHIP_PLAN -> resolveMembershipPlan(gymId, audienceFilter);
            case CUSTOM -> resolveCustom(gymId, audienceFilter);
        };

        return enrichMembersWithUserData(members);
    }

    /**
     * Enrich members with user data (email, names).
     */
    private List<MemberRecipient> enrichMembersWithUserData(List<Member> members) {
        if (members.isEmpty()) {
            return Collections.emptyList();
        }

        // Get all user IDs
        Set<UUID> userIds = members.stream()
                .map(Member::getUserId)
                .collect(Collectors.toSet());

        // Fetch users in batch
        List<User> users = userRepository.findAllById(userIds);
        Map<UUID, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // Build recipient list
        return members.stream()
                .map(member -> {
                    User user = userMap.get(member.getUserId());
                    if (user == null) {
                        log.warn("User not found for member: {}", member.getId());
                        return null;
                    }
                    return new MemberRecipient(
                            member.getId(),
                            user.getId(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getEmail());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get all active members for a gym.
     */
    private List<Member> resolveAllMembers(UUID gymId) {
        List<Member> members = memberRepository.findByGymId(gymId);
        return members.stream()
                .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    /**
     * Get members subscribed to specific classes.
     * Filter format: {"classIds": ["uuid1", "uuid2"]}
     */
    private List<Member> resolveClassSubscribers(UUID gymId, String audienceFilter) {
        // TODO: Integrate with ClassBookingRepository to find members enrolled in
        // specific classes
        log.warn("Class subscribers audience not fully implemented, returning all members");
        return resolveAllMembers(gymId);
    }

    /**
     * Get members with active/upcoming bookings.
     * Filter format: {"dateFrom": "2026-01-01", "dateTo": "2026-12-31"}
     */
    private List<Member> resolveBookingParticipants(UUID gymId, String audienceFilter) {
        // TODO: Integrate with booking system to find members with bookings
        log.warn("Booking participants audience not fully implemented, returning all members");
        return resolveAllMembers(gymId);
    }

    /**
     * Get members on specific membership plans.
     * Filter format: {"planIds": ["uuid1", "uuid2"]}
     */
    private List<Member> resolveMembershipPlan(UUID gymId, String audienceFilter) {
        // TODO: Integrate with MemberMembershipRepository to filter by plan
        log.warn("Membership plan audience not fully implemented, returning all members");
        return resolveAllMembers(gymId);
    }

    /**
     * Custom member selection.
     * Filter format: {"memberIds": ["uuid1", "uuid2"]}
     */
    private List<Member> resolveCustom(UUID gymId, String audienceFilter) {
        // TODO: Parse memberIds from filter and fetch specific members
        log.warn("Custom audience not fully implemented, returning all members");
        return resolveAllMembers(gymId);
    }

    /**
     * Get a preview of the audience without fetching all details.
     */
    @Transactional(readOnly = true)
    public AudiencePreviewResponse getAudiencePreview(UUID gymId, AudienceType audienceType, String audienceFilter) {
        List<MemberRecipient> recipients = resolveAudience(gymId, audienceType, audienceFilter);

        List<AudiencePreviewResponse.RecipientPreview> sampleRecipients = recipients.stream()
                .limit(10)
                .map(r -> AudiencePreviewResponse.RecipientPreview.builder()
                        .memberId(r.memberId())
                        .firstName(r.firstName())
                        .lastName(r.lastName())
                        .email(r.email())
                        .build())
                .collect(Collectors.toList());

        return AudiencePreviewResponse.builder()
                .totalCount(recipients.size())
                .sampleRecipients(sampleRecipients)
                .build();
    }
}
