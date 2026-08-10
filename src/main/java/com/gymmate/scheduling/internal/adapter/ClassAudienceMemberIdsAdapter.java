package com.gymmate.scheduling.internal.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.scheduling.internal.repository.ClassBookingJpaRepository;
import com.gymmate.notification.application.port.AudienceMemberIdsResolver;
import com.gymmate.notification.domain.AudienceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implements notification's {@link AudienceMemberIdsResolver} port for class-related
 * audiences — moved out of {@code notification.application.AudienceResolver} to break
 * a module dependency cycle (see the port package Javadoc).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ClassAudienceMemberIdsAdapter implements AudienceMemberIdsResolver {

    private final ClassBookingJpaRepository classBookingRepository;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(AudienceType type) {
        return type == AudienceType.CLASS_SUBSCRIBERS || type == AudienceType.BOOKING_PARTICIPANTS;
    }

    @Override
    public Set<UUID> resolveMemberIds(UUID gymId, AudienceType type, String audienceFilter) {
        return switch (type) {
            case CLASS_SUBSCRIBERS -> resolveClassSubscribers(gymId, audienceFilter);
            case BOOKING_PARTICIPANTS -> resolveBookingParticipants(gymId, audienceFilter);
            default -> throw new IllegalArgumentException("Unsupported audience type: " + type);
        };
    }

    /**
     * Filter format: {"classIds": ["uuid1", "uuid2"]}
     */
    private Set<UUID> resolveClassSubscribers(UUID gymId, String audienceFilter) {
        try {
            Set<UUID> classScheduleIds = parseUuidListFromFilter(audienceFilter, "classIds");
            if (classScheduleIds.isEmpty()) {
                log.warn("No classIds provided in audience filter, falling back to all members");
                return null;
            }

            return classBookingRepository.findByGymId(gymId).stream()
                    .filter(booking -> classScheduleIds.contains(booking.getClassScheduleId()))
                    .map(booking -> booking.getMemberId())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to resolve class subscribers: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Filter format: {"dateFrom": "2026-01-01", "dateTo": "2026-12-31"}
     */
    private Set<UUID> resolveBookingParticipants(UUID gymId, String audienceFilter) {
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

            final LocalDateTime from = dateFrom;
            final LocalDateTime to = dateTo;
            return classBookingRepository.findByGymId(gymId).stream()
                    .filter(booking -> booking.getBookingDate() != null
                            && !booking.getBookingDate().isBefore(from)
                            && !booking.getBookingDate().isAfter(to))
                    .map(booking -> booking.getMemberId())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to resolve booking participants: {}", e.getMessage());
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
