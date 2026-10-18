package com.gymmate.shared.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;

/**
 * Keeps the JSON binding contract the API had on Jackson 2.
 *
 * <p>Jackson 2 bound request bodies through constructors of any visibility, so DTOs built with
 * Lombok {@code @Data @Builder} (whose all-args constructor is package-private) deserialized
 * without extra annotations. Jackson 3 only considers public creators by default; this restores
 * the previous creator visibility so every existing request DTO keeps working unchanged.
 *
 * <p>Jackson 3 also fails a request whose body omits (or nulls) a primitive field, such as a
 * {@code boolean} component of a request record; Jackson 2 bound it to {@code false}/{@code 0}.
 * Clients rely on omitting optional flags, so the Jackson 2 behaviour is restored too.
 */
@Configuration(proxyBeanMethods = false)
class JacksonConfig {

    @Bean
    JsonMapperBuilderCustomizer jackson2CreatorVisibility() {
        return builder -> builder.changeDefaultVisibility(vc -> vc.withCreatorVisibility(Visibility.ANY));
    }

    @Bean
    JsonMapperBuilderCustomizer jackson2NullForPrimitives() {
        return builder -> builder.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
    }
}
