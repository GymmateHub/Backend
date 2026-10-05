package com.gymmate.architecture;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HTTP contract guard for the Clean Architecture / modular-monolith refactor.
 *
 * <p>Scans every {@code @Controller}/{@code @RestController} under {@code com.gymmate}
 * via reflection (no Spring context, no DB) and compares the resulting set of
 * {@code METHOD /path} routes with a committed snapshot. Controller <em>class names and
 * packages are deliberately excluded</em> from the snapshot, so moving a controller
 * between modules/layers keeps this test green, while adding, removing or changing a
 * route — which would break the frontend or mobile clients — fails it.
 *
 * <p>An intentional API change is accepted by regenerating the snapshot:
 * {@code ./mvnw test -Dtest=ApiEndpointSnapshotTest -DupdateApiSnapshot=true}
 * and committing the diff of {@value #SNAPSHOT}.
 */
class ApiEndpointSnapshotTest {

    static final String SNAPSHOT = "src/test/resources/architecture/api-endpoints.snapshot";
    private static final String BASE_PACKAGE = "com.gymmate";

    @Test
    void httpRoutesMatchCommittedSnapshot() throws IOException {
        List<String> actual = collectRoutes();
        Path snapshot = Path.of(SNAPSHOT);

        if (Boolean.getBoolean("updateApiSnapshot") || Files.notExists(snapshot)) {
            Files.createDirectories(snapshot.getParent());
            Files.write(snapshot, actual, StandardCharsets.UTF_8);
        }

        List<String> expected = Files.readAllLines(snapshot, StandardCharsets.UTF_8);
        assertThat(actual)
                .as("HTTP routes changed. If intentional, regenerate with -DupdateApiSnapshot=true")
                .containsExactlyElementsOf(expected);
    }

    static List<String> collectRoutes() {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));

        var routes = new TreeSet<String>();
        for (var candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            Class<?> type = load(candidate.getBeanClassName());
            RequestMapping typeMapping = AnnotatedElementUtils.findMergedAnnotation(type, RequestMapping.class);
            String[] prefixes = paths(typeMapping);

            for (Method method : type.getMethods()) {
                RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
                if (mapping == null) {
                    continue;
                }
                RequestMethod[] verbs = mapping.method().length > 0
                        ? mapping.method()
                        : (typeMapping != null && typeMapping.method().length > 0 ? typeMapping.method() : null);
                for (String prefix : prefixes) {
                    for (String suffix : paths(mapping)) {
                        String path = normalise(prefix + "/" + suffix);
                        if (verbs == null) {
                            routes.add("ANY " + path);
                        } else {
                            for (RequestMethod verb : verbs) {
                                routes.add(verb.name() + " " + path);
                            }
                        }
                    }
                }
            }
        }
        return new ArrayList<>(routes);
    }

    private static String[] paths(RequestMapping mapping) {
        if (mapping == null || mapping.path().length == 0) {
            return new String[] {""};
        }
        return mapping.path();
    }

    private static String normalise(String raw) {
        String path = ("/" + raw).replaceAll("/+", "/");
        return path.length() > 1 && path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private static Class<?> load(String className) {
        try {
            return Class.forName(className, false, ApiEndpointSnapshotTest.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
