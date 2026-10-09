package com.gymmate.shared.infrastructure.persistence;

import com.gymmate.crm.internal.application.port.LeadRepository;
import com.gymmate.crm.internal.domain.Lead;
import com.gymmate.crm.internal.domain.LeadStatus;
import com.gymmate.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JPA semantics of the domain/JPA data-mapper layer, against PostgreSQL: id and audit write-back,
 * dirty checking without explicit save, identity map, auto-flush before queries, merge of
 * detached domain objects, deletes and read-only transactions.
 */
class DomainPersistenceIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    LeadRepository leads;
    @Autowired
    PlatformTransactionManager txManager;
    @Autowired
    PersistenceMappers mappers;
    @Autowired
    org.springframework.jdbc.core.JdbcTemplate jdbc;

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

    private Lead newLead(String firstName) {
        Lead lead = Lead.builder().firstName(firstName).lastName("Tester").email(firstName + "@example.com")
                .source("walk-in").build();
        lead.setOrganisationId(orgId);
        lead.setGymId(gymId);
        return lead;
    }

    private Lead persisted(String firstName) {
        return tx.execute(s -> leads.save(newLead(firstName)));
    }

    @Test
    void allEntitiesAreMapped() {
        assertThat(mappers.size()).isGreaterThan(0);
    }

    @Test
    void saveWritesGeneratedStateBackOntoTheSameInstance() {
        Lead lead = newLead("Ada");

        Lead returned = tx.execute(s -> leads.save(lead));

        assertThat(returned).isSameAs(lead);
        assertThat(lead.getId()).isNotNull();
        assertThat(lead.getCreatedAt()).isNotNull();
        assertThat(lead.isActive()).isTrue();
    }

    @Test
    void changesToLoadedDomainObjectsAreFlushedWithoutExplicitSave() {
        UUID id = persisted("Grace").getId();

        tx.executeWithoutResult(s -> {
            Lead lead = leads.findById(id).orElseThrow();
            lead.updateStatus(LeadStatus.CONTACTED); // no save()
        });

        Lead reloaded = tx.execute(s -> leads.findById(id).orElseThrow());
        assertThat(reloaded.getStatus()).isEqualTo(LeadStatus.CONTACTED);
    }

    @Test
    void sameRowYieldsSameDomainInstanceWithinATransaction() {
        UUID id = persisted("Linus").getId();

        tx.executeWithoutResult(s -> {
            Lead a = leads.findById(id).orElseThrow();
            Lead b = leads.findById(id).orElseThrow();
            List<Lead> all = leads.findByGymId(gymId);
            assertThat(a).isSameAs(b);
            assertThat(all).anySatisfy(l -> assertThat(l).isSameAs(a));
        });
    }

    @Test
    void queriesSeeUnsavedChangesThroughAutoFlush() {
        UUID id = persisted("Barbara").getId();

        Long contacted = tx.execute(s -> {
            leads.findById(id).orElseThrow().updateStatus(LeadStatus.CONTACTED); // no save()
            return leads.countByOrganisationIdAndStatus(orgId, LeadStatus.CONTACTED);
        });

        assertThat(contacted).isEqualTo(1L);
    }

    @Test
    void detachedDomainObjectsAreMergedOnSave() {
        Lead detached = persisted("Edsger");
        detached.setNotes("updated while detached");

        tx.executeWithoutResult(s -> leads.save(detached));

        String notes = tx.execute(s -> leads.findById(detached.getId()).orElseThrow().getNotes());
        assertThat(notes).isEqualTo("updated while detached");
    }

    @Test
    void deleteRemovesTheRow() {
        Lead lead = persisted("Ken");

        tx.executeWithoutResult(s -> leads.delete(leads.findById(lead.getId()).orElseThrow()));

        java.util.Optional<Lead> after = tx.execute(s -> leads.findById(lead.getId()));
        assertThat(after).isEmpty();
    }

    @Test
    void readOnlyTransactionsDoNotWrite() {
        UUID id = persisted("Margaret").getId();
        TransactionTemplate readOnly = new TransactionTemplate(txManager);
        readOnly.setReadOnly(true);
        readOnly.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        readOnly.executeWithoutResult(s -> leads.findById(id).orElseThrow().setNotes("must not be persisted"));

        String notes = tx.execute(s -> leads.findById(id).orElseThrow().getNotes());
        assertThat(notes).isNull();
    }

    @Test
    void callsOutsideATransactionBehaveLikeSpringDataRepositories() {
        Lead lead = leads.save(newLead("Tony")); // adapter opens its own transaction

        assertThat(lead.getId()).isNotNull();
        assertThat(leads.findById(lead.getId())).get().extracting(Lead::getFirstName).isEqualTo("Tony");
    }

    // ------------------------------------------------------------------ generic CRUD (JpaDomainRepositoryAdapter)

    @Test
    void genericCrudOperationsWorkThroughTheModulePort() {
        Lead a = persisted("Alan");
        Lead b = persisted("Betty");

        tx.executeWithoutResult(s -> {
            assertThat(leads.existsById(a.getId())).isTrue();
            assertThat(leads.findAllById(List.of(a.getId(), b.getId())))
                    .extracting(Lead::getFirstName).containsExactlyInAnyOrder("Alan", "Betty");
            assertThat(leads.count()).isGreaterThanOrEqualTo(2);
            assertThat(leads.findAll()).extracting(Lead::getId).contains(a.getId(), b.getId());

            Page<Lead> page = leads.findAll(PageRequest.of(0, 1));
            assertThat(page.getContent()).hasSize(1).first().isInstanceOf(Lead.class);
            assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        });

        List<Lead> saved = tx.execute(s -> leads.saveAll(List.of(newLead("Claude"), newLead("Dana"))));
        assertThat(saved).allSatisfy(lead -> assertThat(lead.getId()).isNotNull());

        tx.executeWithoutResult(s -> leads.deleteById(a.getId()));
        Boolean exists = tx.execute(s -> leads.existsById(a.getId()));
        assertThat(exists).isFalse();
    }

    // ------------------------------------------------------------------ optimistic locking

    @Test
    void versionStartsAtZeroAndIncrementsOnEveryUpdate() {
        Lead lead = persisted("Linus");
        assertThat(lead.getVersion()).isZero();

        tx.executeWithoutResult(s -> leads.findById(lead.getId()).orElseThrow().updateStatus(LeadStatus.CONTACTED));

        Long version = tx.execute(s -> leads.findById(lead.getId()).orElseThrow().getVersion());
        assertThat(version).isEqualTo(1L);
    }

    @Test
    void savingAStaleCopyIsRejected() {
        UUID id = persisted("Barbara").getId();
        Lead stale = tx.execute(s -> leads.findById(id).orElseThrow());
        tx.executeWithoutResult(s -> leads.findById(id).orElseThrow().updateStatus(LeadStatus.CONTACTED));

        stale.setFirstName("Overwrite");

        assertThatThrownBy(() -> tx.execute(s -> leads.save(stale)))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        String firstName = tx.execute(s -> leads.findById(id).orElseThrow().getFirstName());
        assertThat(firstName).isEqualTo("Barbara");
    }

    @Test
    void concurrentWritersOfTheSameRowDoNotOverwriteEachOther() throws Exception {
        UUID id = persisted("Edsger").getId();
        CountDownLatch bothLoaded = new CountDownLatch(2);
        CountDownLatch firstCommitted = new CountDownLatch(1);

        Callable<String> writer = () -> {
            try {
                tx.executeWithoutResult(s -> {
                    Lead lead = leads.findById(id).orElseThrow();
                    bothLoaded.countDown();
                    await(bothLoaded);
                    lead.setFirstName(Thread.currentThread().getName());
                    if (!Thread.currentThread().getName().equals("first")) {
                        await(firstCommitted);
                    }
                });
                return "committed";
            } catch (ObjectOptimisticLockingFailureException e) {
                return "conflict";
            } finally {
                if (Thread.currentThread().getName().equals("first")) {
                    firstCommitted.countDown();
                }
            }
        };
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<String> first = pool.submit(named("first", writer));
            Future<String> second = pool.submit(named("second", writer));

            assertThat(List.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS)))
                    .containsExactly("committed", "conflict");
        } finally {
            pool.shutdownNow();
        }
        String firstName = tx.execute(s -> leads.findById(id).orElseThrow().getFirstName());
        assertThat(firstName).isEqualTo("first");
    }

    private static <T> Callable<T> named(String name, Callable<T> task) {
        return () -> {
            String previous = Thread.currentThread().getName();
            Thread.currentThread().setName(name);
            try {
                return task.call();
            } finally {
                Thread.currentThread().setName(previous);
            }
        };
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(20, TimeUnit.SECONDS)) {
                throw new IllegalStateException("timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
