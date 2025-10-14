package com.gymmate;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
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

        SpringApplication.run(GymMateApplication.class, args);
    }
}
