package com.gymmate.notification.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.notification.domain.Notification;
import com.gymmate.notification.events.*;
import com.gymmate.notification.infrastructure.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Event listener for domain events that creates admin notifications.
 * Listens to events from across the system and persists them as notifications.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AdminNotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final NotificationDispatcher notificationDispatcher;
    private final ObjectMapper objectMapper;

    /**
     * Handle payment failed events.
     */
    @EventListener
    @Async
    @Transactional
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        log.info("Handling PaymentFailedEvent for organisation: {}", event.getOrganisationId());

        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("gymId", event.getGymId());
            metadata.put("amount", event.getAmount());
            metadata.put("failureReason", event.getFailureReason());
            metadata.put("nextRetryDate", event.getNextRetryDate());
            metadata.put("invoiceId", event.getInvoiceId());

            Notification notification = Notification.builder()
                    .gymId(event.getGymId())
                    .title(event.getNotificationTitle())
                    .message(event.getNotificationMessage())
                    .priority(event.getPriority())
                    .eventType(event.getEventType())
                    .metadata(objectMapper.writeValueAsString(metadata))
                    .relatedEntityId(event.getGymId())
                    .relatedEntityType("GYM")
                    .recipientRole(Notification.RecipientRole.OWNER)
                    .scope(Notification.NotificationScope.GYM)
                    .build();

            Notification saved = notificationRepository.save(notification);
            log.info("Created notification {} for PaymentFailedEvent", saved.getId());

            // Dispatch to SSE
            notificationDispatcher.dispatch(saved);

        } catch (Exception e) {
            log.error("Failed to handle PaymentFailedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle payment success events.
     */
    @EventListener
    @Async
    @Transactional
    public void handlePaymentSuccessEvent(PaymentSuccessEvent event) {
        log.info("Handling PaymentSuccessEvent for organisation: {}", event.getOrganisationId());

        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("gymId", event.getGymId());
            metadata.put("amount", event.getAmount());
            metadata.put("invoiceNumber", event.getInvoiceNumber());
            metadata.put("invoiceUrl", event.getInvoiceUrl());
            metadata.put("periodEnd", event.getPeriodEnd());

            Notification notification = Notification.builder()
                    .gymId(event.getGymId())
                    .title(event.getNotificationTitle())
                    .message(event.getNotificationMessage())
                    .priority(event.getPriority())
                    .eventType(event.getEventType())
                    .metadata(objectMapper.writeValueAsString(metadata))
                    .relatedEntityId(event.getGymId())
                    .relatedEntityType("GYM")
                    .recipientRole(Notification.RecipientRole.OWNER)
                    .scope(Notification.NotificationScope.GYM)
                    .build();

            Notification saved = notificationRepository.save(notification);
            log.info("Created notification {} for PaymentSuccessEvent", saved.getId());

            // Dispatch to SSE
            notificationDispatcher.dispatch(saved);

        } catch (Exception e) {
            log.error("Failed to handle PaymentSuccessEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle subscription expiring events.
     */
    @EventListener
    @Async
    @Transactional
    public void handleSubscriptionExpiringEvent(SubscriptionExpiringEvent event) {
        log.info("Handling SubscriptionExpiringEvent for organisation: {}", event.getOrganisationId());

        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("subscriptionId", event.getSubscriptionId());
            metadata.put("tierName", event.getTierName());
            metadata.put("price", event.getPrice());
            metadata.put("expiresAt", event.getExpiresAt());
            metadata.put("daysUntilExpiry", event.getDaysUntilExpiry());

            Notification notification = Notification.builder()
                    .title(event.getNotificationTitle())
                    .message(event.getNotificationMessage())
                    .priority(event.getPriority())
                    .eventType(event.getEventType())
                    .metadata(objectMapper.writeValueAsString(metadata))
                    .relatedEntityId(event.getSubscriptionId())
                    .relatedEntityType("SUBSCRIPTION")
                    .recipientRole(Notification.RecipientRole.OWNER)
                    .scope(Notification.NotificationScope.ORGANISATION)
                    .build();

            Notification saved = notificationRepository.save(notification);
            log.info("Created notification {} for SubscriptionExpiringEvent", saved.getId());

            // Dispatch to SSE
            notificationDispatcher.dispatch(saved);

        } catch (Exception e) {
            log.error("Failed to handle SubscriptionExpiringEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle member joined events.
     */
    @EventListener
    @Async
    @Transactional
    public void handleMemberJoinedEvent(MemberJoinedEvent event) {
        log.info("Handling MemberJoinedEvent for organisation: {}", event.getOrganisationId());

        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("gymId", event.getGymId());
            metadata.put("memberId", event.getMemberId());
            metadata.put("memberName", event.getMemberName());
            metadata.put("memberEmail", event.getMemberEmail());
            metadata.put("membershipPlan", event.getMembershipPlan());

            Notification notification = Notification.builder()
                    .gymId(event.getGymId())
                    .title(event.getNotificationTitle())
                    .message(event.getNotificationMessage())
                    .priority(event.getPriority())
                    .eventType(event.getEventType())
                    .metadata(objectMapper.writeValueAsString(metadata))
                    .relatedEntityId(event.getMemberId())
                    .relatedEntityType("MEMBER")
                    .recipientRole(Notification.RecipientRole.GYM_MANAGER)
                    .scope(Notification.NotificationScope.GYM)
                    .build();

            Notification saved = notificationRepository.save(notification);
            log.info("Created notification {} for MemberJoinedEvent", saved.getId());

            // Dispatch to SSE
            notificationDispatcher.dispatch(saved);

        } catch (Exception e) {
            log.error("Failed to handle MemberJoinedEvent: {}", e.getMessage(), e);
        }
    }
}
