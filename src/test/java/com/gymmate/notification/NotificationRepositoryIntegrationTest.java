package com.gymmate.notification;

import com.gymmate.notification.internal.application.port.NotificationRepository;
import com.gymmate.notification.internal.domain.Notification;
import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Notification persistence through the repository port (replaces the former pass-through
 * adapter unit test): organisation- and gym-level notifications, unread queries, nested enum
 * mapping and deletes, against PostgreSQL.
 */
class NotificationRepositoryIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    NotificationRepository notifications;
    @Autowired
    PlatformTransactionManager txManager;
    @Autowired
    JdbcTemplate jdbc;

    TransactionTemplate tx;
    UUID orgId;
    UUID gymId;

    @BeforeEach
    void setUp() {
        tx = new TransactionTemplate(txManager);
        orgId = UUID.randomUUID();
        gymId = UUID.randomUUID();
        jdbc.update("insert into organisations (id, name, slug) values (?, ?, ?)", orgId, "Org " + orgId, "org-" + orgId);
        jdbc.update("insert into gyms (id, organisation_id, name, slug) values (?, ?, ?, ?)", gymId, orgId, "Gym", "gym-" + gymId);
    }

    private Notification notification(UUID gym) {
        Notification n = Notification.builder()
                .title("Test Notification")
                .message("Test message")
                .priority(NotificationPriority.HIGH)
                .eventType("TEST_EVENT")
                .recipientRole(Notification.RecipientRole.OWNER)
                .scope(gym != null ? Notification.NotificationScope.GYM : Notification.NotificationScope.ORGANISATION)
                .gymId(gym)
                .build();
        n.setOrganisationId(orgId);
        return n;
    }

    @Test
    void savesOrganisationAndGymNotifications() {
        Notification org = tx.execute(s -> notifications.save(notification(null)));
        Notification gym = tx.execute(s -> notifications.save(notification(gymId)));

        assertThat(org.getId()).isNotNull();
        assertThat(gym.getGymId()).isEqualTo(gymId);
        Optional<Notification> reloaded = tx.execute(s -> notifications.findById(gym.getId()));
        assertThat(reloaded).get().satisfies(n -> {
            assertThat(n.getScope()).isEqualTo(Notification.NotificationScope.GYM);
            assertThat(n.getRecipientRole()).isEqualTo(Notification.RecipientRole.OWNER);
            assertThat(n.getPriority()).isEqualTo(NotificationPriority.HIGH);
        });
    }

    @Test
    void findByIdIsEmptyWhenMissing() {
        Optional<Notification> missing = tx.execute(s -> notifications.findById(UUID.randomUUID()));
        assertThat(missing).isEmpty();
    }

    @Test
    void organisationLevelUnreadQueries() {
        tx.executeWithoutResult(s -> {
            notifications.save(notification(null));
            notifications.save(notification(null));
        });
        UUID readId = tx.execute(s -> notifications.save(notification(null)).getId());
        tx.executeWithoutResult(s -> notifications.findById(readId).orElseThrow().markAsRead()); // flushed without save

        Long unread = tx.execute(s -> notifications.countUnreadByOrganisationId(orgId));
        Page<Notification> page = tx.execute(s -> notifications.findByOrganisationId(orgId, Pageable.unpaged()));
        List<Notification> recent = tx.execute(s -> notifications.findRecentByOrganisationId(orgId, LocalDateTime.now().minusHours(1)));

        assertThat(unread).isEqualTo(2L);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(recent).hasSize(3);
    }

    @Test
    void gymLevelQueries() {
        tx.executeWithoutResult(s -> {
            notifications.save(notification(gymId));
            notifications.save(notification(null));
        });

        Page<Notification> gymPage = tx.execute(s -> notifications.findUnreadByGymId(gymId, Pageable.unpaged()));
        Long gymUnread = tx.execute(s -> notifications.countUnreadByGymId(gymId));

        assertThat(gymPage.getContent()).hasSize(1).allSatisfy(n -> assertThat(n.getGymId()).isEqualTo(gymId));
        assertThat(gymUnread).isEqualTo(1L);
    }

    @Test
    void deletesNotifications() {
        Notification saved = tx.execute(s -> notifications.save(notification(null)));

        tx.executeWithoutResult(s -> notifications.delete(notifications.findById(saved.getId()).orElseThrow()));

        Optional<Notification> after = tx.execute(s -> notifications.findById(saved.getId()));
        assertThat(after).isEmpty();
    }
}
