package com.gymmate.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
          .addServersItem(new Server()
            .url(""))
            .info(new Info()
            .title("GymMate API")
            .description("GymMate SaaS API Documentation")
            .version("1.0.0"));
    }
}
