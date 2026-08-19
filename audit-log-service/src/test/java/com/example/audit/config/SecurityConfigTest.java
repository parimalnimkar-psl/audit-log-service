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

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain filterChain;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtEncoder jwtEncoder;

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
}
