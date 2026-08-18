package com.example.audit.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new user
 */
public record CreateUserRequest(
    @NotBlank(message = "Username is required")
    @JsonProperty("username")
    String username,

    @NotBlank(message = "Password is required")
    @JsonProperty("password")
    String password,

    @NotNull(message = "Role is required")
    @JsonProperty("role")
    String role, // ROLE_ADMIN, ROLE_AUDIT_WRITER, ROLE_AUDIT_READER

    @JsonProperty("description")
    String description
) {}
