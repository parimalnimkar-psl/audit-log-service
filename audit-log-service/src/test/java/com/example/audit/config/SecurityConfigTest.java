package com.example.audit.config;

import static org.junit.jupiter.api.Assertions.*;

import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain filterChain;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private MockMvc mvc;

    @Test
    void securityFilterChainBeanExists() {
        assertNotNull(filterChain);
    }

    @Test
    void jwtDecoderBeanExists() {
        assertNotNull(jwtDecoder);
    }

    @Test
    void jwtEncoderBeanExists() {
        assertNotNull(jwtEncoder);
    }

    @Test
    void jwtDecoderCanDecodeValidToken() throws Exception {
        var now = java.time.Instant.now();
        var claims = JwtClaimsSet.builder().issuer("audit-log-service").subject("test")
            .issuedAt(now).expiresAt(now.plusSeconds(60)).claim("scope", "AUDIT_READER").build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        assertEquals("test", jwtDecoder.decode(token).getSubject());
    }

    @Test
    void jwtEncoderCanEncodeToken() throws Exception {
        var claims = JwtClaimsSet.builder().subject("test").build();
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        assertFalse(jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue().isBlank());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_AUDIT_READER")
    void readerCannotAccessH2ConsoleOrMetrics() throws Exception {
        mvc.perform(get("/h2-console/login.jsp")).andExpect(status().isForbidden());
        mvc.perform(get("/actuator/metrics")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_AUDIT_ADMIN")
    void adminCanAccessProtectedMetrics() throws Exception {
        mvc.perform(get("/actuator/metrics")).andExpect(status().isOk());
    }

    @Test
    void malformedBearerTokenIsRejected() throws Exception {
        mvc.perform(get("/audit/events").header("Authorization", "Bearer malformed"))
            .andExpect(status().isUnauthorized());
    }
}
