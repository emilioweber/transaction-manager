package com.wex.purchasetransaction.config.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wex.purchasetransaction.config.properties.RateLimitProperties;
import com.wex.purchasetransaction.exception.ApiError;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimitProperties properties;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public RateLimitFilter(
            RateLimitProperties properties,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = resolveKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, this::newBucket);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(
                1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill())
        );


        log.warn("Rate limit exceeded [key={}, retryAfter={}s]", key, retryAfterSeconds);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiError error = ApiError.of(
                HttpStatus.TOO_MANY_REQUESTS,
                "Rate limit exceeded. Please retry later."
        );

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }


    private Bucket newBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(properties.getRequests())
                .refillIntervally(
                        properties.getRequests(),
                        Duration.ofSeconds(properties.getDurationSeconds())
                )
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String resolveKey(HttpServletRequest request) {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7); // rate limit per token
        }
        return request.getRemoteAddr(); // fallback per IP
    }
}
