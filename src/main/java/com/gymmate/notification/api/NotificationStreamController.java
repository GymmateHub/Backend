package com.gymmate.notification.api;

import com.gymmate.notification.infrastructure.SseEmitterRegistry;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.security.TenantAwareUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * REST controller for SSE notification streaming.
 * Provides real-time notification delivery to admin dashboard.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Real-time notification APIs")
public class NotificationStreamController {

    private final SseEmitterRegistry sseEmitterRegistry;

    /**
     * Subscribe to real-time notifications via Server-Sent Events.
     * Only accessible to OWNER and ADMIN roles.
     *
     * @return SseEmitter for streaming notifications
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(
            summary = "Subscribe to real-time notifications",
            description = "Opens an SSE connection to receive real-time notifications. " +
                    "Connection includes periodic heartbeats to keep it alive."
    )
    public SseEmitter streamNotifications(@AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        UUID organisationId = userDetails.getOrganisationId();
        UUID userId = userDetails.getUserId();

        log.info("User {} from organisation {} subscribing to notification stream", userId, organisationId);

        // Create SSE connection with 30-minute timeout
        SseEmitter emitter = sseEmitterRegistry.registerConnection(organisationId, userId, 30 * 60 * 1000L);

        log.info("Active SSE connections for organisation {}: {}",
                organisationId,
                sseEmitterRegistry.getConnectionCount(organisationId));

        return emitter;
    }

    /**
     * Get SSE connection statistics (for debugging/monitoring).
     */
    @GetMapping("/stream/stats")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get SSE connection statistics")
    public ConnectionStats getConnectionStats(@AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        UUID organisationId = userDetails.getOrganisationId();

        return new ConnectionStats(
                sseEmitterRegistry.getConnectionCount(organisationId),
                sseEmitterRegistry.getTotalConnectionCount()
        );
    }

    /**
     * DTO for connection statistics.
     */
    public record ConnectionStats(int organisationConnections, int totalConnections) {
    }
}



