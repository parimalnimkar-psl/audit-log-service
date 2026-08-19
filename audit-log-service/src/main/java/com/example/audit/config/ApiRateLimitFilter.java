package com.example.audit.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiRateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_REQUESTS = 120;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/audit/") && !request.getRequestURI().startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }
        String key = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        Window window = windows.compute(key, (ignored, current) ->
            current == null || now - current.startedAt() >= WINDOW.toMillis()
                ? new Window(now, new AtomicInteger(1))
                : current);
        if (window.count().incrementAndGet() > MAX_REQUESTS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", "60");
            return;
        }
        chain.doFilter(request, response);
    }

    private record Window(long startedAt, AtomicInteger count) { }
}