package com.gymmate.retail.internal.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.gymmate.retail.internal.domain.EquipmentCategory;
import com.gymmate.retail.internal.domain.EquipmentStatus;
import com.gymmate.retail.internal.domain.InventoryCategory;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JSON binding of retail enums: lenient, case-insensitive parsing with a default value
 * (the enums' own {@code fromString} factories), attached as Jackson mix-ins so the domain
 * enums stay free of serialization annotations.
 */
@Configuration(proxyBeanMethods = false)
class RetailJacksonConfig {

    @Bean
    JsonMapperBuilderCustomizer retailEnumBinding() {
        return builder -> builder
                .addMixIn(InventoryCategory.class, InventoryCategoryJson.class)
                .addMixIn(EquipmentCategory.class, EquipmentCategoryJson.class)
                .addMixIn(EquipmentStatus.class, EquipmentStatusJson.class);
    }

    abstract static class InventoryCategoryJson {
        @JsonCreator
        static InventoryCategory fromString(String value) {
            return InventoryCategory.fromString(value);
        }
    }

    abstract static class EquipmentCategoryJson {
        @JsonCreator
        static EquipmentCategory fromString(String value) {
            return EquipmentCategory.fromString(value);
        }
    }

    abstract static class EquipmentStatusJson {
        @JsonCreator
        static EquipmentStatus fromString(String value) {
            return EquipmentStatus.fromString(value);
        }
    }
}
