package com.gymmate.notification.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing SSE (Server-Sent Events) connections.
 * Maintains active connections per organisation and user for real-time
 * notifications.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SseEmitterRegistry {

    private final ObjectMapper objectMapper;

    // Map: organisationId -> (userId -> SseEmitter) — for authenticated
    // notification streams
    private final Map<UUID, Map<UUID, ConnectionInfo>> connections = new ConcurrentHashMap<>();

    // Map: userId (string) -> SseEmitter — for unauthenticated, short-lived email
    // status streams
    private final Map<String, SseEmitter> emailStatusEmitters = new ConcurrentHashMap<>();

    private static final long EMAIL_STATUS_TIMEOUT = 60_000L; // 60 seconds

    /**
     * Register a new SSE connection for a user.
     *
     * @param organisationId the organisation ID
     * @param userId         the user ID
     * @param timeout        connection timeout in milliseconds
     * @return configured SseEmitter
     */
    public SseEmitter registerConnection(UUID organisationId, UUID userId, long timeout) {
        SseEmitter emitter = new SseEmitter(timeout);

        // Handle completion and timeout
        emitter.onCompletion(() -> removeConnection(organisationId, userId));
        emitter.onTimeout(() -> {
            log.debug("SSE connection timed out for user {} in organisation {}", userId, organisationId);
            removeConnection(organisationId, userId);
        });
        emitter.onError(throwable -> {
            log.error("SSE error for user {} in organisation {}: {}", userId, organisationId, throwable.getMessage());
            removeConnection(organisationId, userId);
        });

        // Store connection
        connections.computeIfAbsent(organisationId, k -> new ConcurrentHashMap<>())
                .put(userId, new ConnectionInfo(emitter, LocalDateTime.now()));

        log.info("Registered SSE connection for user {} in organisation {}", userId, organisationId);

        // Send initial heartbeat
        sendHeartbeat(emitter, userId);

        return emitter;
    }

    /**
     * Remove a connection.
     */
    private void removeConnection(UUID organisationId, UUID userId) {
        Map<UUID, ConnectionInfo> orgConnections = connections.get(organisationId);
        if (orgConnections != null) {
            orgConnections.remove(userId);
            if (orgConnections.isEmpty()) {
                connections.remove(organisationId);
            }
        }
        log.info("Removed SSE connection for user {} in organisation {}", userId, organisationId);
    }

    /**
     * Send notification to all connected users in an organisation.
     *
     * @return true if at least one user received the notification
     */
    public boolean sendToOrganisation(UUID organisationId, Notification notification) {
        Map<UUID, ConnectionInfo> orgConnections = connections.get(organisationId);
        if (orgConnections == null || orgConnections.isEmpty()) {
            return false;
        }

        boolean sentToAny = false;
        for (Map.Entry<UUID, ConnectionInfo> entry : orgConnections.entrySet()) {
            UUID userId = entry.getKey();
            ConnectionInfo info = entry.getValue();

            try {
                sendNotification(info.emitter, notification);
                info.updateLastActivity();
                sentToAny = true;
                log.debug("Sent notification {} to user {} in organisation {}",
                        notification.getId(), userId, organisationId);
            } catch (Exception e) {
                log.warn("Failed to send notification to user {}: {}", userId, e.getMessage());
                removeConnection(organisationId, userId);
            }
        }

        return sentToAny;
    }

    /**
     * Send notification to a specific user.
     *
     * @return true if the notification was sent
     */
    public boolean sendToUser(UUID organisationId, UUID userId, Notification notification) {
        Map<UUID, ConnectionInfo> orgConnections = connections.get(organisationId);
        if (orgConnections == null) {
            return false;
        }

        ConnectionInfo info = orgConnections.get(userId);
        if (info == null) {
            return false;
        }

        try {
            sendNotification(info.emitter, notification);
            info.updateLastActivity();
            log.debug("Sent notification {} to user {}", notification.getId(), userId);
            return true;
        } catch (Exception e) {
            log.warn("Failed to send notification to user {}: {}", userId, e.getMessage());
            removeConnection(organisationId, userId);
            return false;
        }
    }

    /**
     * Send notification data via SSE.
     */
    private void sendNotification(SseEmitter emitter, Notification notification) throws IOException {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", notification.getId());
        eventData.put("title", notification.getTitle());
        eventData.put("message", notification.getMessage());
        eventData.put("priority", notification.getPriority());
        eventData.put("eventType", notification.getEventType());
        eventData.put("metadata", notification.getMetadata());
        eventData.put("createdAt", notification.getCreatedAt());

        emitter.send(SseEmitter.event()
                .name("notification")
                .data(objectMapper.writeValueAsString(eventData)));
    }

    /**
     * Send heartbeat to keep connection alive.
     */
    private void sendHeartbeat(SseEmitter emitter, UUID userId) {
        try {
            emitter.send(SseEmitter.event()
                    .name("heartbeat")
                    .data("ping"));
            log.trace("Sent heartbeat to user {}", userId);
        } catch (IOException e) {
            log.debug("Failed to send heartbeat to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Scheduled task to send heartbeats and clean up stale connections.
     * Runs every 30 seconds.
     */
    @Scheduled(fixedRate = 30000)
    public void sendHeartbeats() {
        LocalDateTime staleThreshold = LocalDateTime.now().minusMinutes(5);
        List<ConnectionToRemove> staleConnections = new ArrayList<>();

        for (Map.Entry<UUID, Map<UUID, ConnectionInfo>> orgEntry : connections.entrySet()) {
            UUID organisationId = orgEntry.getKey();
            Map<UUID, ConnectionInfo> orgConnections = orgEntry.getValue();

            for (Map.Entry<UUID, ConnectionInfo> userEntry : orgConnections.entrySet()) {
                UUID userId = userEntry.getKey();
                ConnectionInfo info = userEntry.getValue();

                // Check if connection is stale
                if (info.lastActivity.isBefore(staleThreshold)) {
                    log.info("Marking stale connection for removal: user {} in org {}", userId, organisationId);
                    staleConnections.add(new ConnectionToRemove(organisationId, userId));
                    continue;
                }

                // Send heartbeat
                sendHeartbeat(info.emitter, userId);
            }
        }

        // Remove stale connections
        staleConnections.forEach(conn -> removeConnection(conn.organisationId, conn.userId));

        if (!connections.isEmpty()) {
            log.debug("Active SSE connections: {} organisations, {} total users",
                    connections.size(),
                    connections.values().stream().mapToInt(Map::size).sum());
        }
    }

    /**
     * Get count of active connections for an organisation.
     */
    public int getConnectionCount(UUID organisationId) {
        Map<UUID, ConnectionInfo> orgConnections = connections.get(organisationId);
        return orgConnections != null ? orgConnections.size() : 0;
    }

    /**
     * Get total active connection count.
     */
    public int getTotalConnectionCount() {
        return connections.values().stream().mapToInt(Map::size).sum();
    }

    // ==================== EMAIL STATUS (Unauthenticated) ====================

    /**
     * Register a short-lived SSE connection for email delivery status.
     * Used during registration/login before the user is authenticated.
     *
     * @param userId the user ID (as string)
     * @return configured SseEmitter
     */
    public SseEmitter createEmailStatusEmitter(String userId) {
        // Complete any existing emitter for this user
        SseEmitter existing = emailStatusEmitters.remove(userId);
        if (existing != null) {
            existing.complete();
        }

        SseEmitter emitter = new SseEmitter(EMAIL_STATUS_TIMEOUT);

        emitter.onCompletion(() -> {
            log.debug("Email status SSE completed for userId: {}", userId);
            emailStatusEmitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            log.debug("Email status SSE timed out for userId: {}", userId);
            emailStatusEmitters.remove(userId);
        });
        emitter.onError(ex -> {
            log.debug("Email status SSE error for userId: {}", userId);
            emailStatusEmitters.remove(userId);
        });

        emailStatusEmitters.put(userId, emitter);
        log.debug("Registered email status SSE for userId: {}", userId);

        return emitter;
    }

    /**
     * Send an email delivery status event to a client.
     *
     * @param userId  the user to notify
     * @param status  "SENDING", "SENT", or "FAILED"
     * @param message a human-readable message
     */
    public void sendEmailStatus(String userId, String status, String message) {
        SseEmitter emitter = emailStatusEmitters.get(userId);
        if (emitter == null) {
            log.debug("No email status SSE emitter for userId: {} — client may not be listening", userId);
            return;
        }

        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("status", status);
            eventData.put("message", message);

            emitter.send(SseEmitter.event()
                    .name("email-status")
                    .data(objectMapper.writeValueAsString(eventData)));

            // Complete the emitter after a terminal event
            if ("SENT".equals(status) || "FAILED".equals(status)) {
                emitter.complete();
                emailStatusEmitters.remove(userId);
            }
        } catch (IOException e) {
            log.warn("Failed to send email status SSE to userId: {} — client likely disconnected", userId);
            emailStatusEmitters.remove(userId);
        }
    }

    /**
     * Internal class to track connection info.
     */
    private static class ConnectionInfo {
        final SseEmitter emitter;
        LocalDateTime lastActivity;

        ConnectionInfo(SseEmitter emitter, LocalDateTime lastActivity) {
            this.emitter = emitter;
            this.lastActivity = lastActivity;
        }

        void updateLastActivity() {
            this.lastActivity = LocalDateTime.now();
        }
    }

    /**
     * Helper record for tracking connections to remove.
     */
    private record ConnectionToRemove(UUID organisationId, UUID userId) {
    }
}
