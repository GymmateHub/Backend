package com.gymmate.health;

import com.gymmate.health.internal.application.port.ExerciseCategoryRepository;
import com.gymmate.health.internal.domain.ExerciseCategory;
import com.gymmate.support.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Health aggregates are never physically removed: every delete path of their repositories
 * (SoftDeletingJpaDomainRepositoryAdapter) deactivates the row instead.
 */
class HealthSoftDeleteIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    ExerciseCategoryRepository categories;
    @Autowired
    PlatformTransactionManager txManager;
    @Autowired
    JdbcTemplate jdbc;

    private UUID persistedCategory() {
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return tx.execute(s -> categories.save(ExerciseCategory.builder()
                .name("Category " + UUID.randomUUID().toString().substring(0, 8)).build())).getId();
    }

    private Boolean activeFlag(UUID id) {
        return jdbc.queryForObject("select is_active from exercise_categories where id = ?", Boolean.class, id);
    }

    @Test
    void deleteDeactivatesTheRow() {
        UUID id = persistedCategory();
        TransactionTemplate tx = new TransactionTemplate(txManager);

        tx.executeWithoutResult(s -> categories.delete(categories.findById(id).orElseThrow()));

        assertThat(activeFlag(id)).isFalse();
    }

    @Test
    void deleteByIdAlsoDeactivatesInsteadOfRemoving() {
        UUID id = persistedCategory();

        new TransactionTemplate(txManager).executeWithoutResult(s -> categories.deleteById(id));

        assertThat(activeFlag(id)).isFalse();
    }

    @Test
    void deleteByIdOfAMissingRowIsIgnored() {
        new TransactionTemplate(txManager).executeWithoutResult(s -> categories.deleteById(UUID.randomUUID()));
    }
}
