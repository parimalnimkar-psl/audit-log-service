package com.example.audit.config;

import static org.mockito.Mockito.*;

import com.example.audit.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class UserStatusFilterTest {

    @Test
    void inactiveJwtIsRejected() throws Exception {
        UserService users = mock(UserService.class);
        when(users.isUserActive("disabled")).thenReturn(false);
        UserStatusFilter filter = new UserStatusFilter(users);
        var jwt = Jwt.withTokenValue("token")
            .header("alg", "HS256")
            .subject("disabled")
            .claim("scope", "AUDIT_READER")
            .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        MockHttpServletResponse response = new MockHttpServletResponse();
        var chain = mock(jakarta.servlet.FilterChain.class);

        try {
            filter.doFilter(new MockHttpServletRequest(), response, chain);
            org.junit.jupiter.api.Assertions.assertEquals(401, response.getStatus());
            verify(chain, never()).doFilter(any(), any());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void activeJwtContinues() throws Exception {
        UserService users = mock(UserService.class);
        when(users.isUserActive("active")).thenReturn(true);
        UserStatusFilter filter = new UserStatusFilter(users);
        var jwt = Jwt.withTokenValue("token")
            .header("alg", "HS256")
            .subject("active")
            .claim("scope", "AUDIT_READER")
            .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        var chain = mock(jakarta.servlet.FilterChain.class);

        try {
            filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);
            verify(chain).doFilter(any(), any());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
