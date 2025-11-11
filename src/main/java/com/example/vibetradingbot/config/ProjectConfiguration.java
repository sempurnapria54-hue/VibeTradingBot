package com.example.vibetradingbot.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Общая конфигурация приложения.
 */
@Configuration
@EnableConfigurationProperties({
    DecimalProperties.class,
    CanonicalTimeframeProperties.class,
    JwtProperties.class,
    HistoryIngestionProperties.class,
    ConsistencyProperties.class,
    IndicatorProperties.class,
    SignalProperties.class,
    BacktestProperties.class
})
public class ProjectConfiguration {
}
