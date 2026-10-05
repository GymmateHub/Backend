package com.gymmate.shared.events;

import com.gymmate.notification.api.event.MemberJoinedEvent;
import com.gymmate.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Cross-module events handled by {@link AsyncModuleListener}s are delivered after commit, are
 * recorded in (and completed in) the event publication registry, and are not delivered when
 * the publishing transaction rolls back.
 */
class DurableEventDeliveryIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    ApplicationEventPublisher publisher;
    @Autowired
    PlatformTransactionManager txManager;
    @Autowired
    JdbcTemplate jdbc;

    UUID orgId;
    UUID gymId;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        gymId = UUID.randomUUID();
        jdbc.update("insert into organisations (id, name, slug) values (?, ?, ?)", orgId, "Org " + orgId, "org-" + orgId);
        jdbc.update("insert into gyms (id, organisation_id, name, slug) values (?, ?, ?, ?)", gymId, orgId, "Gym", "gym-" + gymId);
    }

    private MemberJoinedEvent memberJoined() {
        return MemberJoinedEvent.builder().organisationId(orgId).gymId(gymId).memberId(UUID.randomUUID())
                .memberName("Ada Lovelace").memberEmail("ada@example.com").membershipPlan("Gold").build();
    }

    private long notificationsForGym() {
        return jdbc.queryForObject("select count(*) from notifications where gym_id = ?", Long.class, gymId);
    }

    @Test
    void deliversAfterCommitAndCompletesThePublication() {
        MemberJoinedEvent event = memberJoined();

        new TransactionTemplate(txManager).executeWithoutResult(s -> publisher.publishEvent(event));

        await().atMost(Duration.ofSeconds(10)).until(() -> notificationsForGym() == 1);
        await().atMost(Duration.ofSeconds(10)).until(() -> jdbc.queryForObject(
                "select count(*) from event_publication where serialized_event like ? and completion_date is not null",
                Long.class, "%" + event.getEventId() + "%") >= 1);
    }

    @Test
    void isNotDeliveredWhenThePublisherRollsBack() throws InterruptedException {
        TransactionTemplate tx = new TransactionTemplate(txManager);

        tx.executeWithoutResult(s -> {
            publisher.publishEvent(memberJoined());
            s.setRollbackOnly();
        });

        Thread.sleep(1500);
        assertThat(notificationsForGym()).isZero();
    }

    @Test
    void eventsPublishedOutsideATransactionAreStillDelivered() {
        publisher.publishEvent(memberJoined());

        await().atMost(Duration.ofSeconds(10)).until(() -> notificationsForGym() == 1);
    }
}
