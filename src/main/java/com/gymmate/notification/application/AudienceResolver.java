package com.gymmate.notification.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.notification.api.dto.AudiencePreviewResponse;
import com.gymmate.notification.application.port.AudienceMemberIdsResolver;
import com.gymmate.notification.application.port.MemberDirectory;
import com.gymmate.notification.domain.AudienceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Resolves target audiences for newsletter campaigns. Type-specific member-ID
 * resolution ({@code CLASS_SUBSCRIBERS}/{@code BOOKING_PARTICIPANTS}/
 * {@code MEMBERSHIP_PLAN}) and member/user enrichment are delegated to
 * {@link AudienceMemberIdsResolver}/{@link MemberDirectory} implementations owned by
 * the modules that actually have that data (classes, membership, user) — see
 * {@code com.gymmate.notification.application.port} package Javadoc for why.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AudienceResolver {

    private final List<AudienceMemberIdsResolver> memberIdsResolvers;
    private final MemberDirectory memberDirectory;
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

        return switch (audienceType) {
            case ALL_MEMBERS -> memberDirectory.findActiveMembersByGym(gymId);
            case CUSTOM -> resolveCustom(gymId, audienceFilter);
            case CLASS_SUBSCRIBERS -> resolveViaIdResolver(gymId, audienceType, audienceFilter, true);
            case BOOKING_PARTICIPANTS, MEMBERSHIP_PLAN -> resolveViaIdResolver(gymId, audienceType, audienceFilter, false);
        };
    }

    /**
     * Delegates to whichever {@link AudienceMemberIdsResolver} supports this type,
     * then enriches the resulting IDs via {@link MemberDirectory}. A {@code null}
     * result from the resolver means "no usable filter" — falls back to all active
     * members, matching the original single-module behavior.
     */
    private List<MemberRecipient> resolveViaIdResolver(UUID gymId, AudienceType type, String audienceFilter, boolean activeOnly) {
        AudienceMemberIdsResolver resolver = memberIdsResolvers.stream()
                .filter(r -> r.supports(type))
                .findFirst()
                .orElse(null);

        if (resolver == null) {
            log.warn("No AudienceMemberIdsResolver registered for {}, falling back to all active members", type);
            return memberDirectory.findActiveMembersByGym(gymId);
        }

        Set<UUID> memberIds = resolver.resolveMemberIds(gymId, type, audienceFilter);
        if (memberIds == null) {
            return memberDirectory.findActiveMembersByGym(gymId);
        }
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }

        return memberDirectory.findMembersByIds(gymId, memberIds, activeOnly);
    }

    /**
     * Custom member selection.
     * Filter format: {"memberIds": ["uuid1", "uuid2"]}
     */
    private List<MemberRecipient> resolveCustom(UUID gymId, String audienceFilter) {
        Set<UUID> memberIds = parseUuidListFromFilter(audienceFilter, "memberIds");
        if (memberIds.isEmpty()) {
            log.warn("No memberIds provided in custom audience filter, returning empty list");
            return Collections.emptyList();
        }
        return memberDirectory.findMembersByIds(gymId, memberIds, false);
    }

    /**
     * Parse a list of UUIDs from a JSON filter string.
     * E.g. {"memberIds": ["uuid1", "uuid2"]} -> Set of UUIDs
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
