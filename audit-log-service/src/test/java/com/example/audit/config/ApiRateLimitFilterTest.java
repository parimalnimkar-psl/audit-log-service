package com.example.audit.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ApiRateLimitFilterTest {

    private ApiRateLimitFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() throws Exception {
        filter = new ApiRateLimitFilter();
        chain = mock(FilterChain.class);
        Field windows = ApiRateLimitFilter.class.getDeclaredField("windows");
        windows.setAccessible(true);
        ((Map<?, ?>) windows.get(filter)).clear();
    }

    @Test
    void allowsRequestsUntilWindowLimitThenReturns429() throws Exception {
        for (int requestNumber = 1; requestNumber <= 120; requestNumber++) {
            MockHttpServletRequest request = request("/audit/events", "192.0.2.10");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, chain);

            assertEquals(200, response.getStatus());
        }

        MockHttpServletResponse limitedResponse = new MockHttpServletResponse();
        filter.doFilter(request("/audit/events", "192.0.2.10"), limitedResponse, chain);

        assertEquals(429, limitedResponse.getStatus());
        assertEquals("60", limitedResponse.getHeader("Retry-After"));
        verify(chain, times(120)).doFilter(any(), any());
    }

    @Test
    void tracksClientsIndependently() throws Exception {
        for (int requestNumber = 1; requestNumber <= 120; requestNumber++) {
            filter.doFilter(request("/api/users", "192.0.2.11"), new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse otherClientResponse = new MockHttpServletResponse();
        filter.doFilter(request("/api/users", "192.0.2.12"), otherClientResponse, chain);

        assertEquals(200, otherClientResponse.getStatus());
        verify(chain, times(121)).doFilter(any(), any());
    }

    @Test
    void doesNotRateLimitPublicNonApiPaths() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request("/actuator/health", "192.0.2.10"), response, chain);

        assertEquals(200, response.getStatus());
        verify(chain).doFilter(any(), any());
    }

    private MockHttpServletRequest request(String path, String remoteAddress) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        request.setRemoteAddr(remoteAddress);
        return request;
    }
}
