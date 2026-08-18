package com.example.audit.api;

import com.example.audit.api.dto.CreateUserRequest;
import com.example.audit.api.dto.UserResponse;
import com.example.audit.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Management API Controller
 * Endpoints for creating and managing audit system users
 * Requires ROLE_ADMIN for all operations
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a new user
     * Only admins can create users
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new user", description = "Creates a new audit system user. Requires ADMIN role.")
    public ResponseEntity<UserResponse> createUser(
        @Valid @RequestBody CreateUserRequest request,
        Authentication auth
    ) {
        String createdBy = auth != null ? auth.getName() : "SYSTEM";
        UserResponse response = userService.createUser(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Retrieves user information. Requires ADMIN role.")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get user by username
     */
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by username", description = "Retrieves user information by username. Requires ADMIN role.")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * List all active users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all active users", description = "Lists all active users in the system. Requires ADMIN role.")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userService.listActiveUsers());
    }

    /**
     * List users by role
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List users by role", description = "Lists all users with a specific role. Requires ADMIN role.")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable String role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    /**
     * Update user role
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role", description = "Changes the role of an existing user. Requires ADMIN role.")
    public ResponseEntity<UserResponse> updateUserRole(
        @PathVariable Long id,
        @RequestParam String newRole,
        Authentication auth
    ) {
        String updatedBy = auth != null ? auth.getName() : "SYSTEM";
        UserResponse response = userService.updateUserRole(id, newRole, updatedBy);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate user
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user", description = "Deactivates a user account (soft delete). Requires ADMIN role.")
    public ResponseEntity<UserResponse> deactivateUser(
        @PathVariable Long id,
        Authentication auth
    ) {
        String deactivatedBy = auth != null ? auth.getName() : "SYSTEM";
        UserResponse response = userService.deactivateUser(id, deactivatedBy);
        return ResponseEntity.ok(response);
    }

    /**
     * Reactivate user
     */
    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivate user", description = "Reactivates a deactivated user account. Requires ADMIN role.")
    public ResponseEntity<UserResponse> reactivateUser(
        @PathVariable Long id,
        Authentication auth
    ) {
        String reactivatedBy = auth != null ? auth.getName() : "SYSTEM";
        UserResponse response = userService.reactivateUser(id, reactivatedBy);
        return ResponseEntity.ok(response);
    }
}
