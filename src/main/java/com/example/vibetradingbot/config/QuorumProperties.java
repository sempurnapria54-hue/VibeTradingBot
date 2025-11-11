package com.example.vibetradingbot.config;

import java.math.RoundingMode;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.example.vibetradingbot.util.Constants;

import lombok.Getter;
import lombok.Setter;

/**
 * Конфигурация кворумного движка (debounce, cooldown и округление score).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = Constants.Config.QUORUM_PREFIX)
public class QuorumProperties {

    private Integer debounceBars;
    private Integer cooldownBars;
    private Numeric numeric = new Numeric();

    @Getter
    @Setter
    public static class Numeric {

        private Integer scoreScale;
        private RoundingMode rounding = RoundingMode.HALF_UP;
    }
}
