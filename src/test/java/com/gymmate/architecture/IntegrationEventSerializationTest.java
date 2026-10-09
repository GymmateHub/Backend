package com.gymmate.architecture;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.gymmate.identity.api.event.MemberOnboardedEvent;
import com.gymmate.notification.api.event.PaymentFailedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every public integration event ({@code *.api.event.*}) must survive a JSON round trip:
 * the Spring Modulith event publication registry stores events as JSON and deserializes them
 * when republishing incomplete publications.
 */
class IntegrationEventSerializationTest {

    private final ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void allIntegrationEventsRoundTripThroughJson() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile("com\\.gymmate\\.\\w+\\.api\\.event\\.\\w+Event")));
        List<Class<?>> events = new ArrayList<>();
        for (BeanDefinition bd : scanner.findCandidateComponents("com.gymmate")) {
            events.add(Class.forName(bd.getBeanClassName()));
        }
        assertThat(events).hasSizeGreaterThanOrEqualTo(12);

        for (Class<?> type : events) {
            Object event = sample(type);
            String json = mapper.writeValueAsString(event);
            Object back = mapper.readValue(json, type);
            assertThat(mapper.writeValueAsString(back)).as(type.getSimpleName()).isEqualTo(json);
        }
    }

    @Test
    void preservesPayloadValues() throws Exception {
        UUID org = UUID.randomUUID();
        PaymentFailedEvent event = new PaymentFailedEvent(
                org,
                null,
                new BigDecimal("12.50"),
                "card_declined",
                LocalDateTime.of(2026, 1, 2, 3, 4),
                null,
                null,
                null);

        PaymentFailedEvent back = mapper.readValue(mapper.writeValueAsString(event), PaymentFailedEvent.class);

        assertThat(back.getEventId()).isEqualTo(event.getEventId());
        assertThat(back.getOrganisationId()).isEqualTo(org);
        assertThat(back.amount()).isEqualByComparingTo("12.50");
        assertThat(back.nextRetryDate()).isEqualTo(event.nextRetryDate());

        MemberOnboardedEvent onboarded = new MemberOnboardedEvent(org, UUID.randomUUID(), UUID.randomUUID(), new String[]{"strength"});
        MemberOnboardedEvent onboardedBack = mapper.readValue(mapper.writeValueAsString(onboarded), MemberOnboardedEvent.class);
        assertThat(onboardedBack.getFitnessGoals()).containsExactly("strength");
        assertThat(onboardedBack.getTenantIdentity()).isEqualTo(onboarded.getTenantIdentity());
    }

    private static Object sample(Class<?> type) throws Exception {
        try {
            Method builder = type.getMethod("builder");
            Object b = builder.invoke(null);
            return b.getClass().getMethod("build").invoke(b);
        } catch (NoSuchMethodException e) {
            if (type.isRecord()) {
                var components = type.getRecordComponents();
                Class<?>[] types = new Class<?>[components.length];
                Object[] values = new Object[components.length];
                for (int i = 0; i < components.length; i++) {
                    types[i] = components[i].getType();
                    values[i] = types[i] == UUID.class ? UUID.randomUUID() : primitiveDefault(types[i]);
                }
                return type.getDeclaredConstructor(types).newInstance(values);
            }
            return new MemberOnboardedEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new String[0]);
        }
    }

    private static Object primitiveDefault(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        return java.lang.reflect.Array.get(java.lang.reflect.Array.newInstance(type, 1), 0);
    }
}
