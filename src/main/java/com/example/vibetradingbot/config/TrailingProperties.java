package com.example.vibetradingbot.config;

import com.example.vibetradingbot.util.Constants;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Настройки трейлинга.
 */
@Data
@Validated
@ConfigurationProperties(prefix = Constants.Config.TRAILING_PREFIX)
public class TrailingProperties {

    private boolean enabled = true;
    private int atrPeriod = 14;
    private double trailKAtr = 1.0;
    private double breakevenAtR = 1.0;
}
