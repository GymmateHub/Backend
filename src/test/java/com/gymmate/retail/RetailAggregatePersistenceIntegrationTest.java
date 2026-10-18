package com.gymmate.retail;

import com.gymmate.retail.internal.application.port.SaleRepository;
import com.gymmate.retail.internal.domain.Sale;
import com.gymmate.retail.internal.domain.SaleItem;
import com.gymmate.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The Sale aggregate (sale + items, cascade + orphan removal, bidirectional association)
 * through the domain/JPA mapper layer.
 */
class RetailAggregatePersistenceIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    SaleRepository sales;
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

    private SaleItem item(String name, int qty, String price) {
        SaleItem item = SaleItem.builder().itemName(name).quantity(qty).unitPrice(new BigDecimal(price)).build();
        item.setOrganisationId(orgId);
        item.setGymId(gymId);
        return item;
    }

    private Sale newSale() {
        Sale sale = Sale.builder().saleNumber("S-" + UUID.randomUUID()).build();
        sale.setOrganisationId(orgId);
        sale.setGymId(gymId);
        return sale;
    }

    @Test
    void cascadesItemsAndWritesBackGeneratedIdsAndDerivedTotals() {
        Sale sale = newSale();
        sale.addItem(item("Protein bar", 2, "3.50"));
        sale.addItem(item("Water", 1, "1.00"));

        tx.executeWithoutResult(s -> sales.save(sale));

        assertThat(sale.getId()).isNotNull();
        assertThat(sale.getItems()).allSatisfy(i -> assertThat(i.getId()).isNotNull());
        BigDecimal persistedLineTotal = jdbc.queryForObject(
                "select line_total from pos_sale_items where sale_id = ? and item_name = 'Protein bar'",
                BigDecimal.class, sale.getId());
        assertThat(persistedLineTotal).isEqualByComparingTo("7.00");
    }

    @Test
    void itemChangesOnALoadedSaleAreFlushedIncludingOrphanRemoval() {
        Sale sale = newSale();
        sale.addItem(item("Protein bar", 2, "3.50"));
        sale.addItem(item("Water", 1, "1.00"));
        tx.executeWithoutResult(s -> sales.save(sale));

        tx.executeWithoutResult(s -> {
            Sale loaded = sales.findById(sale.getId()).orElseThrow();
            assertThat(loaded.getItems()).hasSize(2);
            SaleItem water = loaded.getItems().stream().filter(i -> i.getItemName().equals("Water")).findFirst().orElseThrow();
            loaded.getItems().remove(water); // orphan
            loaded.addItem(item("Towel", 1, "5.00"));
            // no explicit save: dirty checking must cascade the new item and delete the orphan
        });

        List<String> names = jdbc.queryForList(
                "select item_name from pos_sale_items where sale_id = ? order by item_name", String.class, sale.getId());
        assertThat(names).containsExactly("Protein bar", "Towel");
    }

    @Test
    void loadedItemsReferenceTheirLoadedSale() {
        Sale sale = newSale();
        sale.addItem(item("Protein bar", 2, "3.50"));
        tx.executeWithoutResult(s -> sales.save(sale));

        tx.executeWithoutResult(s -> {
            Sale loaded = sales.findById(sale.getId()).orElseThrow();
            assertThat(loaded.getItems().get(0).getSale()).isSameAs(loaded);
        });
    }
}
