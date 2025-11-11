package com.example.vibetradingbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.example.vibetradingbot.util.Constants;

import lombok.Getter;
import lombok.Setter;

/**
 * Свойства расположения конфигурации таймфреймов.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = Constants.Config.CANONICAL_TIMEFRAMES_PREFIX)
public class CanonicalTimeframeProperties {

    private String resource;
}
