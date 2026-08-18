package com.example.audit.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AppPropertiesTest {

    @Autowired
    private AppProperties appProperties;

    @Test
    void appPropertiesBeanExists() {
        assertNotNull(appProperties);
    }

    @Test
    void jwtPropertiesAreLoaded() {
        assertNotNull(appProperties.jwt());
        assertNotNull(appProperties.jwt().secret());
        assertNotNull(appProperties.jwt().issuer());
        assertTrue(appProperties.jwt().expirationMinutes() > 0);
    }

    @Test
    void auditPropertiesAreLoaded() {
        assertNotNull(appProperties.audit());
        assertNotNull(appProperties.audit().genesisHash());
        assertTrue(appProperties.audit().retentionDays() > 0);
    }

    @Test
    void jwtSecretHasMinimumLength() {
        // JWT secret should be at least 32 characters for HS256
        assertTrue(appProperties.jwt().secret().length() >= 32);
    }

    @Test
    void genesisHashIsProvided() {
        assertFalse(appProperties.audit().genesisHash().isEmpty());
    }
}
