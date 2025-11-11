package com.example.vibetradingbot.config;

import java.math.RoundingMode;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.example.vibetradingbot.util.Constants;

import lombok.Getter;
import lombok.Setter;

/**
 * Конфигурация генерации сигналов (debounce, cooldown, фильтры и округление).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = Constants.Config.SIGNALS_PREFIX)
public class SignalProperties {

    private Integer debounceBars;
    private Integer cooldownBars;
    private HtfFilter htfFilter = new HtfFilter();
    private Numeric numeric = new Numeric();

    @Getter
    @Setter
    public static class HtfFilter {

        private Boolean enabled = Boolean.FALSE;
        private String htf;
    }

    @Getter
    @Setter
    public static class Numeric {

        private Integer scoreScale;
        private RoundingMode rounding = RoundingMode.HALF_UP;
    }
}
