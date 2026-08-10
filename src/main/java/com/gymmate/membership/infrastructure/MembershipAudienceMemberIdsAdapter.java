package com.gymmate.membership.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.membership.domain.MembershipStatus;
import com.gymmate.notification.application.port.AudienceMemberIdsResolver;
import com.gymmate.notification.domain.AudienceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implements notification's {@link AudienceMemberIdsResolver} port for the
 * membership-plan audience — moved out of
 * {@code notification.application.AudienceResolver} to break a module dependency
 * cycle (see the port package Javadoc).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MembershipAudienceMemberIdsAdapter implements AudienceMemberIdsResolver {

    private final MemberMembershipJpaRepository memberMembershipRepository;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(AudienceType type) {
        return type == AudienceType.MEMBERSHIP_PLAN;
    }

    /**
     * Filter format: {"planIds": ["uuid1", "uuid2"]}
     */
    @Override
    public Set<UUID> resolveMemberIds(UUID gymId, AudienceType type, String audienceFilter) {
        try {
            Set<UUID> planIds = parseUuidListFromFilter(audienceFilter, "planIds");
            if (planIds.isEmpty()) {
                log.warn("No planIds provided in audience filter, falling back to all members");
                return null;
            }

            return memberMembershipRepository.findByGymId(gymId).stream()
                    .filter(mm -> mm.getMembershipPlanId() != null && planIds.contains(mm.getMembershipPlanId()))
                    .filter(mm -> mm.getStatus() == MembershipStatus.ACTIVE)
                    .map(mm -> mm.getMemberId())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to resolve membership plan audience: {}", e.getMessage());
            return null;
        }
    }

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
}
