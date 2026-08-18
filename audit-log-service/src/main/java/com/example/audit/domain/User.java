package com.example.audit.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * User entity for authentication and authorization
 * Users have roles: ROLE_ADMIN, ROLE_AUDIT_WRITER, ROLE_AUDIT_READER
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username", unique = true),
    @Index(name = "idx_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String role; // ROLE_ADMIN, ROLE_AUDIT_WRITER, ROLE_AUDIT_READER

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(length = 255)
    private String createdBy;

    @Column(length = 255)
    private String description;

    public User(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
