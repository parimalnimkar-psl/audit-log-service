package com.example.audit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI auditOpenAPI() {
        return new OpenAPI().info(new Info().title("Audit Log Service API")
                .version("1.0.0").description("Tamper-evident append-only audit service"))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
