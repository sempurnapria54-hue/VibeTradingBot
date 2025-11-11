package com.example.vibetradingbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.example.vibetradingbot.util.Constants;

import lombok.Getter;
import lombok.Setter;

/**
 * Свойства контроля целостности данных.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = Constants.Config.CONSISTENCY_PREFIX)
public class ConsistencyProperties {

    private int scanWindowBars;
    private int refillChunkBars;
}
