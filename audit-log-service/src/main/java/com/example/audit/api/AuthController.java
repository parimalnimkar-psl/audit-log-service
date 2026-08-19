package com.example.audit.api;

import com.example.audit.config.AppProperties;
import com.example.audit.domain.User;
import com.example.audit.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Profile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
@Profile("!keycloak")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final int MAX_FAILED_ATTEMPTS = 6;
    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(5);
    private final JwtEncoder encoder;
    private final AppProperties props;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, Deque<Long>> failedLoginAttempts = new ConcurrentHashMap<>();

    public AuthController(JwtEncoder e, AppProperties p, UserRepository ur, PasswordEncoder pe) {
        encoder = e;
        props = p;
        userRepository = ur;
        passwordEncoder = pe;
    }

    public record Login(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(max = 200) String password) {
    }

    public record Token(
        @JsonProperty("accessToken") String accessToken,
        @JsonProperty("tokenType") String tokenType,
        @JsonProperty("expiresIn") long expiresIn,
        @JsonProperty("scope") String scope) {
    }

    @PostMapping("/token")
    @Operation(summary = "Generate JWT token", description = "Authenticates user and returns JWT token for API access")
    public ResponseEntity<?> token(@Valid @RequestBody Login login) {
        String userName = login == null || login.username() == null ? "" : login.username().trim();
        if (userName.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "invalid_credentials", "error_description", "Username is required."));
        }

        if (isRateLimited(userName)) {
            log.warn("auth_rate_limited username={}", userName);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "rate_limited", "error_description", "Too many failed attempts. Please try again later."));
        }

        Optional<User> userOpt = userRepository.findByUsernameAndActiveTrue(userName);
        if (userOpt.isEmpty()) {
            recordFailedAttempt(userName);
            log.warn("auth_failed username={} reason=user_not_found", userName);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "invalid_credentials", "error_description", "Invalid username or password"));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(login.password(), user.getPasswordHash())) {
            recordFailedAttempt(userName);
            log.warn("auth_failed username={} reason=invalid_password", userName);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "invalid_credentials", "error_description", "Invalid username or password"));
        }

        clearFailedAttempts(userName);
        String scope = switch (user.getRole()) {
            case "ROLE_AUDIT_WRITER" -> "AUDIT_WRITER AUDIT_READER";
            case "ROLE_AUDIT_READER" -> "AUDIT_READER";
            case "ROLE_ADMIN" -> "AUDIT_WRITER AUDIT_READER AUDIT_ADMIN AUDIT_EXPORTER";
            default -> "AUDIT_READER";
        };

        Instant now = Instant.now();
        long exp = props.jwt().expirationMinutes() * 60;
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(props.jwt().issuer())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(exp))
            .subject(userName)
            .claim("scope", scope)
            .claim("role", user.getRole())
            .claim("userId", user.getId())
            .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String tokenValue = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        log.info("auth_success username={} role={}", userName, user.getRole());
        return ResponseEntity.ok(new Token(tokenValue, "Bearer", exp, scope));
    }

    private boolean isRateLimited(String username) {
        Deque<Long> attempts = failedLoginAttempts.get(username);
        if (attempts == null || attempts.isEmpty()) {
            return false;
        }
        long now = System.currentTimeMillis();
        while (!attempts.isEmpty() && now - attempts.peekFirst() > FAILURE_WINDOW.toMillis()) {
            attempts.pollFirst();
        }
        return attempts.size() >= MAX_FAILED_ATTEMPTS;
    }

    private void recordFailedAttempt(String username) {
        Deque<Long> attempts = failedLoginAttempts.computeIfAbsent(username, ignored -> new ArrayDeque<>());
        long now = System.currentTimeMillis();
        while (!attempts.isEmpty() && now - attempts.peekFirst() > FAILURE_WINDOW.toMillis()) {
            attempts.pollFirst();
        }
        attempts.addLast(now);
        while (attempts.size() > MAX_FAILED_ATTEMPTS) {
            attempts.pollFirst();
        }
    }

    private void clearFailedAttempts(String username) {
        failedLoginAttempts.remove(username);
    }
}
