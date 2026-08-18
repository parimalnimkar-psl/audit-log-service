package com.example.audit.config;

import static org.junit.jupiter.api.Assertions.*;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class OpenApiConfigTest {

    @Autowired
    private OpenAPI openAPI;

    @Test
    void openAPIBeanExists() {
        assertNotNull(openAPI);
    }

    @Test
    void openAPIHasCorrectTitle() {
        assertNotNull(openAPI.getInfo());
        assertEquals("Audit Log Service API", openAPI.getInfo().getTitle());
    }

    @Test
    void openAPIHasCorrectVersion() {
        assertNotNull(openAPI.getInfo());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    void openAPIHasDescription() {
        assertNotNull(openAPI.getInfo());
        assertEquals(
                "Tamper-evident append-only audit service", openAPI.getInfo().getDescription());
    }

    @Test
    void openAPIHasBearerAuthSecurityScheme() {
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());

        SecurityScheme bearerAuth = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertNotNull(bearerAuth);
        assertEquals(SecurityScheme.Type.HTTP, bearerAuth.getType());
        assertEquals("bearer", bearerAuth.getScheme());
        assertEquals("JWT", bearerAuth.getBearerFormat());
    }
}
