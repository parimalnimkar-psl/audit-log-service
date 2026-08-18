package com.example.audit.service;

import com.example.audit.api.dto.CreateUserRequest;
import com.example.audit.api.dto.UserResponse;
import com.example.audit.domain.User;
import com.example.audit.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service for user management operations
 * Handles user creation, updates, and queries
 */
@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new user
     * @param request User creation request
     * @param createdBy Username of the admin creating this user
     * @return Created user response
     * @throws IllegalArgumentException if username already exists or role is invalid
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request, String createdBy) {
        // Validate username doesn't already exist
        if (userRepository.findByUsername(request.username()).isPresent()) {
            log.warn("user_creation_failed username={} reason=already_exists", request.username());
            throw new IllegalArgumentException("Username already exists: " + request.username());
        }

        // Validate role
        if (!isValidRole(request.role())) {
            log.warn("user_creation_failed username={} reason=invalid_role role={}", request.username(), request.role());
            throw new IllegalArgumentException("Invalid role: " + request.role());
        }

        // Hash password using Spring Security's password encoder
        String hashedPassword = passwordEncoder.encode(request.password());

        // Create user
        User user = new User(request.username(), hashedPassword, request.role());
        user.setActive(true);
        user.setCreatedBy(createdBy);
        user.setDescription(request.description());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        User savedUser = userRepository.save(user);
        log.info("user_created id={} username={} role={} createdBy={}", 
            savedUser.getId(), savedUser.getUsername(), savedUser.getRole(), createdBy);

        return UserResponse.from(savedUser);
    }

    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserByUsername(String username) {
        return userRepository.findByUsername(username).map(UserResponse::from);
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id).map(UserResponse::from);
    }

    /**
     * List all active users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> listActiveUsers() {
        return userRepository.findByActiveTrue().stream()
            .map(UserResponse::from)
            .toList();
    }

    /**
     * List users by role
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(String role) {
        return userRepository.findByRole(role).stream()
            .map(UserResponse::from)
            .toList();
    }

    /**
     * Update user role
     */
    @Transactional
    public UserResponse updateUserRole(Long userId, String newRole, String updatedBy) {
        if (!isValidRole(newRole)) {
            throw new IllegalArgumentException("Invalid role: " + newRole);
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        String oldRole = user.getRole();
        user.setRole(newRole);
        user.setUpdatedAt(Instant.now());
        User updated = userRepository.save(user);

        log.info("user_role_updated id={} username={} oldRole={} newRole={} updatedBy={}", 
            userId, user.getUsername(), oldRole, newRole, updatedBy);

        return UserResponse.from(updated);
    }

    /**
     * Deactivate user (soft delete)
     */
    @Transactional
    public UserResponse deactivateUser(Long userId, String deactivatedBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!user.getActive()) {
            throw new IllegalArgumentException("User is already inactive: " + userId);
        }

        user.setActive(false);
        user.setUpdatedAt(Instant.now());
        User updated = userRepository.save(user);

        log.info("user_deactivated id={} username={} deactivatedBy={}", 
            userId, user.getUsername(), deactivatedBy);

        return UserResponse.from(updated);
    }

    /**
     * Reactivate user
     */
    @Transactional
    public UserResponse reactivateUser(Long userId, String reactivatedBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (user.getActive()) {
            throw new IllegalArgumentException("User is already active: " + userId);
        }

        user.setActive(true);
        user.setUpdatedAt(Instant.now());
        User updated = userRepository.save(user);

        log.info("user_reactivated id={} username={} reactivatedBy={}", 
            userId, user.getUsername(), reactivatedBy);

        return UserResponse.from(updated);
    }

    /**
     * Verify user exists and is active
     */
    @Transactional(readOnly = true)
    public boolean isUserActive(String username) {
        return userRepository.findByUsernameAndActiveTrue(username).isPresent();
    }

    /**
     * Validate role against allowed roles
     */
    private boolean isValidRole(String role) {
        return role != null && (
            role.equals("ROLE_ADMIN") ||
            role.equals("ROLE_AUDIT_WRITER") ||
            role.equals("ROLE_AUDIT_READER")
        );
    }
}
