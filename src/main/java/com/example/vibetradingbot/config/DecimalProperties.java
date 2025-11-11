package com.example.vibetradingbot.config;

import java.math.RoundingMode;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.example.vibetradingbot.util.Constants;

import lombok.Getter;
import lombok.Setter;

/**
 * Свойства настройки десятичных значений.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = Constants.Config.DECIMAL_PREFIX)
public class DecimalProperties {

    private int scale;
    private int precision;
    private RoundingMode roundingMode;
}
