package com.franco.backend.security.ratelimit;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@Service
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean allow(String key, int limitPerMinute) {

        Bucket bucket = buckets.computeIfAbsent(key, k -> {

            Bandwidth limit = Bandwidth.builder()
                .capacity(limitPerMinute)
                .refillGreedy(limitPerMinute, Duration.ofMinutes(1))
                .build();

            return Bucket.builder()
                .addLimit(limit)
                .build();
        });

        return bucket.tryConsume(1);
    }
}
