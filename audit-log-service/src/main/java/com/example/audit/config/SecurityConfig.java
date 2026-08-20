package com.example.audit.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.*;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@Profile("!keycloak")
public class SecurityConfig {
    private final UserStatusFilter userStatusFilter;

    public SecurityConfig(UserStatusFilter userStatusFilter) {
        this.userStatusFilter = userStatusFilter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(a -> a
                .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/h2-console/**").hasAuthority("SCOPE_AUDIT_ADMIN")
                .requestMatchers("/actuator/metrics").hasAuthority("SCOPE_AUDIT_ADMIN")
                .anyRequest().authenticated())
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))
            .oauth2ResourceServer(o -> o.jwt(j -> {}))
            .addFilterAfter(userStatusFilter, BearerTokenAuthenticationFilter.class)
            .build();
    }

    @Bean
    PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return resolver -> resolver.setMaxPageSize(200);
    }

    private SecretKey key(AppProperties p) {
        if (p.jwt() == null || p.jwt().secret() == null || p.jwt().secret().length() < 32) {
            throw new IllegalStateException("APP_JWT_SECRET must be set to at least 32 characters");
        }
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
