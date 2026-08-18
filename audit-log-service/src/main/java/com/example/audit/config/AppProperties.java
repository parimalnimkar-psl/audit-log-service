package com.example.audit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Audit audit) {
    public record Jwt(String secret, String issuer, long expirationMinutes) {
    }

    public record Audit(String genesisHash, int retentionDays) {
    }
}
