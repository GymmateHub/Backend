package com.gymmate.notification.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.classes.infrastructure.ClassBookingJpaRepository;
import com.gymmate.membership.infrastructure.MemberMembershipJpaRepository;
import com.gymmate.notification.api.dto.AudiencePreviewResponse;
import com.gymmate.notification.domain.AudienceType;
import com.gymmate.user.domain.Member;
import com.gymmate.shared.constants.MemberStatus;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.MemberRepository;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final ClassBookingJpaRepository classBookingRepository;
    private final MemberMembershipJpaRepository memberMembershipRepository;
    private final ObjectMapper objectMapper;

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
        try {
            Set<UUID> classScheduleIds = parseUuidListFromFilter(audienceFilter, "classIds");
            if (classScheduleIds.isEmpty()) {
                log.warn("No classIds provided in audience filter, returning all members");
                return resolveAllMembers(gymId);
            }

            // Get all bookings for this gym and filter by matching class schedule IDs
            Set<UUID> memberIds = classBookingRepository.findByGymId(gymId).stream()
                    .filter(booking -> classScheduleIds.contains(booking.getClassScheduleId()))
                    .map(booking -> booking.getMemberId())
                    .collect(Collectors.toSet());

            if (memberIds.isEmpty()) {
                return Collections.emptyList();
            }

            return memberRepository.findAllById(memberIds).stream()
                    .filter(m -> m.getGymId().equals(gymId))
                    .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to resolve class subscribers: {}", e.getMessage());
            return resolveAllMembers(gymId);
        }
    }

    /**
     * Get members with active/upcoming bookings.
     * Filter format: {"dateFrom": "2026-01-01", "dateTo": "2026-12-31"}
     */
    private List<Member> resolveBookingParticipants(UUID gymId, String audienceFilter) {
        try {
            LocalDateTime dateFrom = LocalDateTime.now().minusMonths(1);
            LocalDateTime dateTo = LocalDateTime.now().plusMonths(1);

            if (audienceFilter != null && !audienceFilter.isBlank()) {
                JsonNode node = objectMapper.readTree(audienceFilter);
                if (node.has("dateFrom")) {
                    dateFrom = LocalDateTime.parse(node.get("dateFrom").asText() + "T00:00:00");
                }
                if (node.has("dateTo")) {
                    dateTo = LocalDateTime.parse(node.get("dateTo").asText() + "T23:59:59");
                }
            }

            // Find distinct members with bookings in the date range for this gym
            final LocalDateTime from = dateFrom;
            final LocalDateTime to = dateTo;
            Set<UUID> memberIds = classBookingRepository.findByGymId(gymId).stream()
                    .filter(booking -> booking.getBookingDate() != null
                            && !booking.getBookingDate().isBefore(from)
                            && !booking.getBookingDate().isAfter(to))
                    .map(booking -> booking.getMemberId())
                    .collect(Collectors.toSet());

            if (memberIds.isEmpty()) {
                return Collections.emptyList();
            }

            return memberRepository.findAllById(memberIds).stream()
                    .filter(m -> m.getGymId().equals(gymId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to resolve booking participants: {}", e.getMessage());
            return resolveAllMembers(gymId);
        }
    }

    /**
     * Get members on specific membership plans.
     * Filter format: {"planIds": ["uuid1", "uuid2"]}
     */
    private List<Member> resolveMembershipPlan(UUID gymId, String audienceFilter) {
        try {
            Set<UUID> planIds = parseUuidListFromFilter(audienceFilter, "planIds");
            if (planIds.isEmpty()) {
                log.warn("No planIds provided in audience filter, returning all members");
                return resolveAllMembers(gymId);
            }

            // Get all memberships for this gym and filter by plan
            Set<UUID> memberIds = memberMembershipRepository.findByGymId(gymId).stream()
                    .filter(mm -> mm.getMembershipPlanId() != null && planIds.contains(mm.getMembershipPlanId()))
                    .filter(mm -> mm.getStatus() == com.gymmate.membership.domain.MembershipStatus.ACTIVE)
                    .map(mm -> mm.getMemberId())
                    .collect(Collectors.toSet());

            if (memberIds.isEmpty()) {
                return Collections.emptyList();
            }

            return memberRepository.findAllById(memberIds).stream()
                    .filter(m -> m.getGymId().equals(gymId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to resolve membership plan audience: {}", e.getMessage());
            return resolveAllMembers(gymId);
        }
    }

    /**
     * Custom member selection.
     * Filter format: {"memberIds": ["uuid1", "uuid2"]}
     */
    private List<Member> resolveCustom(UUID gymId, String audienceFilter) {
        try {
            Set<UUID> memberIds = parseUuidListFromFilter(audienceFilter, "memberIds");
            if (memberIds.isEmpty()) {
                log.warn("No memberIds provided in custom audience filter, returning empty list");
                return Collections.emptyList();
            }

            return memberRepository.findAllById(memberIds).stream()
                    .filter(m -> m.getGymId().equals(gymId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to resolve custom audience: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Parse a list of UUIDs from a JSON filter string.
     * E.g. {"classIds": ["uuid1", "uuid2"]} → Set of UUIDs
     */
    private Set<UUID> parseUuidListFromFilter(String audienceFilter, String fieldName) {
        if (audienceFilter == null || audienceFilter.isBlank()) {
            return Collections.emptySet();
        }
        try {
            JsonNode node = objectMapper.readTree(audienceFilter);
            JsonNode arrayNode = node.get(fieldName);
            if (arrayNode == null || !arrayNode.isArray()) {
                return Collections.emptySet();
            }
            Set<UUID> ids = new HashSet<>();
            for (JsonNode element : arrayNode) {
                ids.add(UUID.fromString(element.asText()));
            }
            return ids;
        } catch (Exception e) {
            log.error("Failed to parse UUID list from filter field '{}': {}", fieldName, e.getMessage());
            return Collections.emptySet();
        }
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
