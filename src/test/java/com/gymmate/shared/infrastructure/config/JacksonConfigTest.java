package com.gymmate.shared.infrastructure.config;

import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/** Request DTOs built with Lombok {@code @Data @Builder} still bind from JSON on Jackson 3. */
class JacksonConfigTest {

    /** Same shape as the API's request DTOs: no default constructor, package-private all-args. */
    @Data
    @Builder
    static class HubRequest {
        private String name;
        private String contactEmail;
    }

    @Test
    void bindsBuilderDtosThroughTheirPackagePrivateConstructor() {
        JsonMapper.Builder builder = JsonMapper.builder();
        new JacksonConfig().jackson2CreatorVisibility().customize(builder);

        HubRequest request = builder.build()
                .readValue("{\"name\":\"Iron Temple\",\"contactEmail\":\"owner@example.com\"}", HubRequest.class);

        assertThat(request.getName()).isEqualTo("Iron Temple");
        assertThat(request.getContactEmail()).isEqualTo("owner@example.com");
    }
}
