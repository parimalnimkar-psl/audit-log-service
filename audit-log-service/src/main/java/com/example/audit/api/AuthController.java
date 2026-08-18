package com.example.audit.api;

import com.example.audit.config.AppProperties;
import com.example.audit.domain.User;
import com.example.audit.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final JwtEncoder encoder;
    private final AppProperties props;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtEncoder e, AppProperties p, UserRepository ur, PasswordEncoder pe) {
        encoder = e;
        props = p;
        userRepository = ur;
        passwordEncoder = pe;
    }

    public record Login(String username, String password) {
    }

    public record Token(String access_token, String token_type, long expires_in, String scope) {
    }

    @PostMapping("/token")
    @Operation(summary = "Generate JWT token", description = "Authenticates user and returns JWT token for API access")
    public ResponseEntity<?> token(@RequestBody Login login) {
        // Find user by username
        Optional<User> userOpt = userRepository.findByUsernameAndActiveTrue(login.username());
        
        if (userOpt.isEmpty()) {
            log.warn("auth_failed username={} reason=user_not_found", login.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "invalid_credentials", "error_description", "Invalid username or password"));
        }
        
        User user = userOpt.get();
        
        // Verify password
        if (!passwordEncoder.matches(login.password(), user.getPasswordHash())) {
            log.warn("auth_failed username={} reason=invalid_password", login.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "invalid_credentials", "error_description", "Invalid username or password"));
        }
        
        // Generate scope based on user role
        String scope = switch (user.getRole()) {
            case "ROLE_AUDIT_WRITER" -> "AUDIT_WRITER AUDIT_READER";
            case "ROLE_AUDIT_READER" -> "AUDIT_READER";
            case "ROLE_ADMIN" -> "AUDIT_WRITER AUDIT_READER AUDIT_ADMIN";
            default -> "AUDIT_READER";
        };
        
        // Build JWT
        Instant now = Instant.now();
        long exp = props.jwt().expirationMinutes() * 60;
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(props.jwt().issuer())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(exp))
            .subject(login.username())
            .claim("scope", scope)
            .claim("role", user.getRole())
            .claim("userId", user.getId())
            .build();
        
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String tokenValue = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        
        log.info("auth_success username={} role={}", login.username(), user.getRole());
        
        return ResponseEntity.ok(new Token(tokenValue, "Bearer", exp, scope));
    }
}
