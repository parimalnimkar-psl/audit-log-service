package com.example.audit;

import com.example.audit.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class AuditLogApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuditLogApplication.class, args);
    }
}
