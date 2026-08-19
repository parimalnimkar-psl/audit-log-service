package com.example.audit.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new user
 */
public record CreateUserRequest(
    @NotBlank(message = "Username is required")
    @Size(max = 100)
    @Pattern(regexp = "[^\\r\\n]*", message = "Username must not contain line breaks")
    @JsonProperty("username")
    String username,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 200)
    @JsonProperty("password")
    String password,

    @NotNull(message = "Role is required")
    @Pattern(regexp = "ROLE_(ADMIN|AUDIT_WRITER|AUDIT_READER)")
    @JsonProperty("role")
    String role, // ROLE_ADMIN, ROLE_AUDIT_WRITER, ROLE_AUDIT_READER

    @Size(max = 255)
    @Pattern(regexp = "[^\\r\\n]*", message = "Description must not contain line breaks")
    @JsonProperty("description")
    String description
) {}
