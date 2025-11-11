package com.example.vibetradingbot.config;

import com.example.vibetradingbot.util.Constants;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Настройки сверки с биржей.
 */
@Data
@Validated
@ConfigurationProperties(prefix = Constants.Config.RECONCILE_PREFIX)
public class ReconcileProperties {

    private boolean enabled = true;
    private int periodSec = 30;
}
