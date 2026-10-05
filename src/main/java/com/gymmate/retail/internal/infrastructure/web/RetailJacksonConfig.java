package com.gymmate.retail.internal.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.gymmate.retail.internal.domain.EquipmentCategory;
import com.gymmate.retail.internal.domain.EquipmentStatus;
import com.gymmate.retail.internal.domain.InventoryCategory;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
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
    Jackson2ObjectMapperBuilderCustomizer retailEnumBinding() {
        return builder -> builder
                .mixIn(InventoryCategory.class, InventoryCategoryJson.class)
                .mixIn(EquipmentCategory.class, EquipmentCategoryJson.class)
                .mixIn(EquipmentStatus.class, EquipmentStatusJson.class);
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
