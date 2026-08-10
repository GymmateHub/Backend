package com.gymmate.membership.application;

import com.gymmate.membership.domain.MemberInvoice;
import com.gymmate.membership.domain.MemberInvoiceStatus;
import com.gymmate.membership.domain.MembershipStatus;
import com.gymmate.membership.infrastructure.MemberInvoiceRepository;
import com.gymmate.membership.infrastructure.MemberMembershipJpaRepository;
import com.gymmate.notification.events.PaymentFailedEvent;
import com.gymmate.notification.events.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Reacts to Stripe Connect (member payment) outcomes by updating membership state and
 * recording invoices — this logic used to live directly in
 * {@code payment.application.StripeWebhookService}, writing straight into
 * {@code MemberMembershipJpaRepository}/{@code MemberInvoiceRepository}. That made
 * {@code payment} depend on {@code membership} for the write, while {@code membership}
 * separately depends on {@code payment} to initiate Stripe charges (via
 * {@code MemberPaymentService} -> {@code StripeConnectService}) — a real module cycle
 * {@code ApplicationModules.verify()} correctly rejects.
 *
 * <p>Deliberately {@code @EventListener}, NOT {@code @Async}: the original code ran
 * synchronously inside {@code StripeWebhookService}'s transaction, so a failure here
 * failed the whole webhook and let Stripe redeliver (see
 * {@code StripeWebhookService#processConnectWebhook}). A plain (non-async)
 * {@code ApplicationEventPublisher.publishEvent} call is synchronous and propagates
 * exceptions back to the publisher, preserving that behavior exactly — going
 * {@code @Async} here would silently drop that safety net.
 *
 * <p>Only reacts when {@link PaymentSuccessEvent#getMembershipId()} /
 * {@link PaymentFailedEvent#getMembershipId()} is set — these events are also
 * published for platform (organisation-level subscription) payments, which have no
 * membership to update.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MembershipPaymentEventListener {

    private final MemberMembershipJpaRepository memberMembershipRepository;
    private final MemberInvoiceRepository memberInvoiceRepository;

    @EventListener
    @Transactional
    public void handlePaymentSucceeded(PaymentSuccessEvent event) {
        if (event.getMembershipId() == null) {
            return;
        }

        memberMembershipRepository.findById(event.getMembershipId()).ifPresent(membership -> {
            // PAST_DUE goes through clearPastDue() specifically so the grace-period
            // clock (see MembershipService.escalatePastDueMemberships) resets —
            // otherwise a membership that pays successfully while PAST_DUE would
            // still get suspended later since nothing else clears pastDueSince.
            if (membership.getStatus() == MembershipStatus.PAST_DUE) {
                membership.clearPastDue();
                memberMembershipRepository.save(membership);
                log.info("Membership {} reactivated after payment", membership.getId());
            } else if (membership.getStatus() == MembershipStatus.EXPIRED
                    || membership.getStatus() == MembershipStatus.CANCELLED) {
                membership.setStatus(MembershipStatus.ACTIVE);
                memberMembershipRepository.save(membership);
                log.info("Membership {} reactivated after payment", membership.getId());
            }

            MemberInvoice invoice = MemberInvoice.builder()
                    .memberId(membership.getMemberId())
                    .membershipId(membership.getId())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .status(MemberInvoiceStatus.PAID)
                    .description("Membership payment via Stripe Connect")
                    .paidAt(LocalDateTime.now())
                    .build();
            invoice.setGymId(membership.getGymId());
            invoice.setOrganisationId(membership.getOrganisationId());
            memberInvoiceRepository.save(invoice);
        });
    }

    @EventListener
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        if (event.getMembershipId() == null) {
            return;
        }

        memberMembershipRepository.findById(event.getMembershipId()).ifPresent(membership -> {
            membership.markPastDue();
            memberMembershipRepository.save(membership);
            log.warn("Membership {} marked as PAST_DUE due to payment failure", membership.getId());

            MemberInvoice invoice = MemberInvoice.builder()
                    .memberId(membership.getMemberId())
                    .membershipId(membership.getId())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .status(MemberInvoiceStatus.PAYMENT_FAILED)
                    .description("Payment failed: " + event.getFailureReason())
                    .build();
            invoice.setGymId(membership.getGymId());
            invoice.setOrganisationId(membership.getOrganisationId());
            memberInvoiceRepository.save(invoice);
        });
    }
}
