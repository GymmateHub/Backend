package com.gymmate.shared.infrastructure.config;

import com.gymmate.retail.internal.application.dto.InventoryItemCreateRequest;
import com.gymmate.retail.internal.application.dto.MaintenanceRecordCreateRequest;
import com.gymmate.retail.internal.application.dto.MaintenanceScheduleCreateRequest;
import com.gymmate.whitelabel.internal.application.dto.WhitelabelSettingsRequest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Request records with primitive components must bind when the client omits those fields,
 * as they did on Jackson 2 (the dashboard's Add Product request omits the tracking flags).
 */
class JacksonConfigTest {

    private final JsonMapper mapper = mapper();

    private static JsonMapper mapper() {
        JacksonConfig config = new JacksonConfig();
        JsonMapper.Builder builder = JsonMapper.builder();
        config.jackson2CreatorVisibility().customize(builder);
        config.jackson2NullForPrimitives().customize(builder);
        return builder.build();
    }

    @Test
    void omittedPrimitiveFlagsDefaultToFalse() {
        InventoryItemCreateRequest item = mapper.readValue(
                "{\"name\":\"Whey\",\"category\":\"SUPPLEMENTS\",\"unitPrice\":25}", InventoryItemCreateRequest.class);
        assertThat(item.expiryTracking()).isFalse();
        assertThat(item.batchTracking()).isFalse();

        assertThat(mapper.readValue("{}", MaintenanceRecordCreateRequest.class).completed()).isFalse();
        assertThat(mapper.readValue("{}", MaintenanceScheduleCreateRequest.class).recurring()).isFalse();
        assertThat(mapper.readValue("{}", WhitelabelSettingsRequest.class).smtpEnabled()).isFalse();
    }

    @Test
    void explicitNullForPrimitiveIsTreatedAsDefault() {
        InventoryItemCreateRequest item = mapper.readValue(
                "{\"name\":\"Whey\",\"expiryTracking\":null}", InventoryItemCreateRequest.class);
        assertThat(item.expiryTracking()).isFalse();
    }
}
