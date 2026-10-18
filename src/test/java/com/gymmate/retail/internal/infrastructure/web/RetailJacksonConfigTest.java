package com.gymmate.retail.internal.infrastructure.web;

import tools.jackson.databind.ObjectMapper;
import com.gymmate.retail.internal.domain.EquipmentCategory;
import com.gymmate.retail.internal.domain.EquipmentStatus;
import com.gymmate.retail.internal.domain.InventoryCategory;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/** The retail enum mix-ins keep the lenient JSON parsing the enums had via @JsonCreator. */
class RetailJacksonConfigTest {

    private final ObjectMapper mapper;

    RetailJacksonConfigTest() {
        JsonMapper.Builder builder = JsonMapper.builder();
        new RetailJacksonConfig().retailEnumBinding().customize(builder);
        mapper = builder.build();
    }

    @Test
    void parsesCaseInsensitively() throws Exception {
        assertThat(mapper.readValue("\"supplements\"", InventoryCategory.class)).isEqualTo(InventoryCategory.SUPPLEMENTS);
        assertThat(mapper.readValue("\" available \"", EquipmentStatus.class)).isEqualTo(EquipmentStatus.AVAILABLE);
    }

    @Test
    void fallsBackToDefaultsForUnknownValues() throws Exception {
        assertThat(mapper.readValue("\"bogus\"", InventoryCategory.class)).isEqualTo(InventoryCategory.OTHER);
        assertThat(mapper.readValue("\"bogus\"", EquipmentCategory.class)).isEqualTo(EquipmentCategory.OTHER);
    }
}
