package com.gymmate.notification.internal.infrastructure.messaging;

import com.gymmate.shared.events.AsyncModuleListener;
import com.gymmate.notification.internal.application.NotificationDispatcher;
import com.gymmate.notification.api.event.MemberJoinedEvent;
import com.gymmate.notification.api.event.PaymentFailedEvent;
import com.gymmate.notification.api.event.PaymentSuccessEvent;
import com.gymmate.notification.api.event.SubscriptionExpiringEvent;
import tools.jackson.databind.ObjectMapper;
import com.gymmate.notification.internal.domain.Notification;
import com.gymmate.notification.internal.application.port.NotificationRepository;
import com.gymmate.shared.multitenancy.TenantScope;
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
    @AsyncModuleListener
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        log.info("Handling PaymentFailedEvent for organisation: {}", event.getOrganisationId());

        try (TenantScope ignored = TenantScope.activate(event.getOrganisationId(), event.gymId())) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("gymId", event.gymId());
            metadata.put("amount", event.amount());
            metadata.put("failureReason", event.failureReason());
            metadata.put("nextRetryDate", event.nextRetryDate());
            metadata.put("invoiceId", event.invoiceId());

            // A platform (organisation-level) subscription failure has no single gym
            // (event.getGymId() is null — see StripeWebhookService.handleInvoicePaymentFailed);
            // a Connect (member payment) failure always has one. Scope the in-app
            // notification accordingly instead of forcing GYM scope with a null id.
            boolean isGymScoped = event.gymId() != null;
            Notification notification = Notification.builder()
                    .gymId(event.gymId())
                    .title(event.getNotificationTitle())
                    .message(event.getNotificationMessage())
                    .priority(event.getPriority())
                    .eventType(event.getEventType())
                    .metadata(objectMapper.writeValueAsString(metadata))
                    .relatedEntityId(isGymScoped ? event.gymId() : event.getOrganisationId())
                    .relatedEntityType(isGymScoped ? "GYM" : "ORGANISATION")
                    .recipientRole(Notification.RecipientRole.OWNER)
                    .scope(isGymScoped ? Notification.NotificationScope.GYM : Notification.NotificationScope.ORGANISATION)
                    .build();

            Notification saved = notificationRepository.save(notification);
            log.info("Created notification {} for PaymentFailedEvent", saved.getId());

            // Dispatch to SSE
            notificationDispatcher.dispatch(saved);

            // The failure email itself is sent from StripeWebhookService directly
            // (same module as PaymentNotificationService) rather than from here —
            // notification calling back into payment would create a module cycle
            // (payment already depends on gym, gym on user, user on notification).

        } catch (Exception e) {
            log.error("Failed to handle PaymentFailedEvent: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle payment success events.
     */
    @AsyncModuleListener
    public void handlePaymentSuccessEvent(PaymentSuccessEvent event) {
        log.info("Handling PaymentSuccessEvent for organisation: {}", event.getOrganisationId());

        try (TenantScope ignored = TenantScope.activate(event.getOrganisationId(), event.gymId())) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("gymId", event.gymId());
            metadata.put("amount", event.amount());
            metadata.put("invoiceNumber", event.invoiceNumber());
            metadata.put("invoiceUrl", event.invoiceUrl());
            metadata.put("periodEnd", event.periodEnd());

            Notification notification = Notification.builder()
                    .gymId(event.gymId())
                    .title(event.getNotificationTitle())
                    .message(event.getNotificationMessage())
                    .priority(event.getPriority())
                    .eventType(event.getEventType())
                    .metadata(objectMapper.writeValueAsString(metadata))
                    .relatedEntityId(event.gymId())
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
    @AsyncModuleListener
    public void handleSubscriptionExpiringEvent(SubscriptionExpiringEvent event) {
        log.info("Handling SubscriptionExpiringEvent for organisation: {}", event.getOrganisationId());

        try (TenantScope ignored = TenantScope.activate(event.getOrganisationId(), null)) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("subscriptionId", event.subscriptionId());
            metadata.put("tierName", event.tierName());
            metadata.put("price", event.price());
            metadata.put("expiresAt", event.expiresAt());
            metadata.put("daysUntilExpiry", event.daysUntilExpiry());

            Notification notification = Notification.builder()
                    .title(event.getNotificationTitle())
                    .message(event.getNotificationMessage())
                    .priority(event.getPriority())
                    .eventType(event.getEventType())
                    .metadata(objectMapper.writeValueAsString(metadata))
                    .relatedEntityId(event.subscriptionId())
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
    @AsyncModuleListener
    public void handleMemberJoinedEvent(MemberJoinedEvent event) {
        log.info("Handling MemberJoinedEvent for organisation: {}", event.getOrganisationId());

        try (TenantScope ignored = TenantScope.activate(event.getOrganisationId(), event.gymId())) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("gymId", event.gymId());
            metadata.put("memberId", event.memberId());
            metadata.put("memberName", event.memberName());
            metadata.put("memberEmail", event.memberEmail());
            metadata.put("membershipPlan", event.membershipPlan());

            Notification notification = Notification.builder()
                    .gymId(event.gymId())
                    .title(event.getNotificationTitle())
                    .message(event.getNotificationMessage())
                    .priority(event.getPriority())
                    .eventType(event.getEventType())
                    .metadata(objectMapper.writeValueAsString(metadata))
                    .relatedEntityId(event.memberId())
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
