package com.example.vibetradingbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.example.vibetradingbot.util.Constants;

import lombok.Getter;
import lombok.Setter;

/**
 * Свойства управления загрузкой исторических свечей.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = Constants.Config.HISTORY_PREFIX)
public class HistoryIngestionProperties {

    private int batchLimit;
    private long requestTimeoutMs;
    private int maxRetries;
    private long retryBackoffMs;
    private long pageDelayMs;
    private long rateLimitPerSecond;
}
