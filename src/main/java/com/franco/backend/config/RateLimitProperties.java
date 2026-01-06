package com.franco.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.rate-limit")
public record RateLimitProperties(
    int loginPerMinute,
    int refreshPerMinute
) {}
