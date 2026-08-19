package com.gymmate;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableScheduling;

@Modulithic
@SpringBootApplication
@EnableScheduling
public class GymMateApplication {
    public static void main(String[] args) {
        // Load .env file and set system properties
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        // Set all environment variables as system properties
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        // BUG-034: Spring's relaxed binding maps SCREAMING_SNAKE_CASE -> dot-case only for
        // real OS environment variables, not for System properties set programmatically above.
        // A literal system property named "SPRING_PROFILES_ACTIVE" is therefore invisible to
        // spring.profiles.active resolution, so .env's SPRING_PROFILES_ACTIVE was silently
        // ignored by every ./run.sh start (app always fell back to the default profile).
        String activeProfiles = dotenv.get("SPRING_PROFILES_ACTIVE");
        if (activeProfiles != null && !activeProfiles.isBlank()) {
            System.setProperty("spring.profiles.active", activeProfiles);
        }

        SpringApplication.run(GymMateApplication.class, args);
    }
}
