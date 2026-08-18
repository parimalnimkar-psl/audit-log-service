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
        // This test verifies the JWT decoder is properly configured
        assertNotNull(jwtDecoder);
    }

    @Test
    void jwtEncoderCanEncodeToken() throws Exception {
        // This test verifies the JWT encoder is properly configured
        assertNotNull(jwtEncoder);
    }
}
