package com.franco.backend.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.franco.backend.security.ratelimit.RateLimitService;

class RateLimitServiceTest {

    @Test
    void shouldAllowRequestsUntilLimitIsExceeded() {
        RateLimitService service = new RateLimitService();

        String key = "login:127.0.0.1";
        int limit = 5;

        for (int i = 0; i < limit; i++) {
            assertThat(service.allow(key, limit)).isTrue();
        }

        assertThat(service.allow(key, limit)).isFalse();
    }
}
