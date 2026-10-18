package com.gymmate.notification;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every Thymeleaf template the code renders must exist. The password-reset template was
 * missing, so reset emails failed silently in a background thread while the API answered
 * "Password reset email sent".
 */
class EmailTemplatesExistTest {

    private static final Pattern PROCESS_CALL = Pattern.compile("templateEngine\\.process\\(\"([a-z0-9/-]+)\"");

    @Test
    void everyRenderedTemplateExists() throws IOException {
        Set<String> referenced = new TreeSet<>();
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher m = PROCESS_CALL.matcher(Files.readString(file));
                while (m.find()) referenced.add(m.group(1));
            }
        }
        assertThat(referenced).isNotEmpty();
        assertThat(referenced)
                .allSatisfy(name -> assertThat(Path.of("src/main/resources/templates", name + ".html"))
                        .as("template %s", name).exists());
    }
}
