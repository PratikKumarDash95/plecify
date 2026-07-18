package com.campusconnect.portal.security;

import com.campusconnect.portal.common.response.ApiError;
import com.campusconnect.portal.common.response.ApiResponse;
import com.campusconnect.portal.config.props.SecurityProperties;
import com.campusconnect.portal.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-client IP token-bucket rate limiter (Bucket4j, in-memory). Authentication endpoints get
 * a tighter bucket to blunt credential-stuffing; everything else shares a general bucket.
 *
 * <p>In-memory buckets suit a single instance or sticky sessions. For a horizontally scaled
 * deployment, swap the {@link ConcurrentHashMap} for a distributed store (e.g. Redis via
 * bucket4j-redis) — the filter contract stays the same.
 */
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final SecurityProperties.RateLimit config;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Bucket> generalBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> authBuckets = new ConcurrentHashMap<>();

    public RateLimitFilter(SecurityProperties.RateLimit config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String key = clientKey(request);
        boolean isAuth = request.getRequestURI().startsWith("/api/v1/auth/");
        Bucket bucket = isAuth
                ? authBuckets.computeIfAbsent(key, k -> newBucket(config.authCapacity()))
                : generalBuckets.computeIfAbsent(key, k -> newBucket(config.capacity()));

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for {} on {}", key, request.getRequestURI());
            writeTooManyRequests(request, response);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/actuator");
    }

    private Bucket newBucket(long capacity) {
        Refill refill = Refill.greedy(config.refillTokens(),
                Duration.ofSeconds(config.refillDurationSeconds()));
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.failure(
                ErrorCode.RATE_LIMIT_EXCEEDED.getDefaultMessage(),
                ApiError.builder()
                        .code(ErrorCode.RATE_LIMIT_EXCEEDED.name())
                        .status(HttpStatus.TOO_MANY_REQUESTS.value())
                        .build(),
                request.getRequestURI());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
