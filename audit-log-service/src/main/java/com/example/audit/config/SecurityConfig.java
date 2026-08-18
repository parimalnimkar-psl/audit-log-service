package com.example.audit.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(a -> a.requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health", "/actuator/info", "/h2-console/**").permitAll().anyRequest().authenticated()).headers(h -> h.frameOptions(f -> f.sameOrigin())).oauth2ResourceServer(o -> o.jwt(j -> {
        })).build();
    }

    private SecretKey key(AppProperties p) {
        return new SecretKeySpec(p.jwt().secret().getBytes(), "HmacSHA256");
    }

    @Bean
    JwtDecoder jwtDecoder(AppProperties p) {
        return NimbusJwtDecoder.withSecretKey(key(p)).build();
    }

    @Bean
    JwtEncoder jwtEncoder(AppProperties p) {
        return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(key(p)));
    }
}
