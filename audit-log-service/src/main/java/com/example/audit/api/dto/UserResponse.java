package com.example.audit.api.dto;

import com.example.audit.domain.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Response DTO for user information
 */
public record UserResponse(
    @JsonProperty("id")
    Long id,

    @JsonProperty("username")
    String username,

    @JsonProperty("role")
    String role,

    @JsonProperty("active")
    Boolean active,

    @JsonProperty("createdAt")
    Instant createdAt,

    @JsonProperty("updatedAt")
    Instant updatedAt,

    @JsonProperty("createdBy")
    String createdBy,

    @JsonProperty("description")
    String description
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.getActive(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getCreatedBy(),
            user.getDescription()
        );
    }
}
