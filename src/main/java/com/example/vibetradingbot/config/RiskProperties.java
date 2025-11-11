package com.example.vibetradingbot.config;

import com.example.vibetradingbot.util.Constants;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Настройки риск-менеджмента.
 */
@Data
@Validated
@ConfigurationProperties(prefix = Constants.Config.RISK_PREFIX)
public class RiskProperties {

    private double riskPerTradePct = 1.0;
    private int leverageMax = 10;
    private boolean isolated = true;
    private int maxConcurrentPositions = 3;
    private double dailyLossLimitR = 5.0;
    private int maxLosingStreak = 3;
}
