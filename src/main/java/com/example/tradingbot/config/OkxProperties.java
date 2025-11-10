package com.example.tradingbot.config;

import java.time.Duration;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "okx")
public class OkxProperties {
    private String baseUrl;
    private String apiKey;
    private String secretKey;
    private String passphrase;
    private Map<String, String> timeframes;
    private RateLimit rateLimits = new RateLimit();

    @Getter
    @Setter
    public static class RateLimit {
        private int candlesPerSecond = 5;
        private Duration restTimeout = Duration.ofSeconds(5);
        private int retryAttempts = 3;
    }
}
