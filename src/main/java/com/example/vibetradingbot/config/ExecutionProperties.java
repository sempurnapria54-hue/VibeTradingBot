package com.example.vibetradingbot.config;

import com.example.vibetradingbot.domain.enums.ExecutionMode;
import com.example.vibetradingbot.util.Constants;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Настройки исполнения заявок.
 */
@Data
@Validated
@ConfigurationProperties(prefix = Constants.Config.EXECUTION_PREFIX)
public class ExecutionProperties {

    private ExecutionMode mode = ExecutionMode.PAPER;
    private int requestTimeoutMs = 10000;
    private int maxRetries = 3;
    private int retryBackoffMs = 500;
    private int rateLimitPerSecond = 4;
}
