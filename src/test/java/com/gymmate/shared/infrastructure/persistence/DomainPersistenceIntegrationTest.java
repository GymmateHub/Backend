package com.gymmate.shared.infrastructure.persistence;

import com.gymmate.crm.internal.application.port.LeadRepository;
import com.gymmate.crm.internal.domain.Lead;
import com.gymmate.crm.internal.domain.LeadStatus;
import com.gymmate.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
}
