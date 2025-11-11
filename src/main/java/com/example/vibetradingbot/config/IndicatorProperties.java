package com.example.vibetradingbot.config;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.example.vibetradingbot.util.Constants;

import lombok.Getter;
import lombok.Setter;

/**
 * Конфигурация индикаторов: warmup, размеры батчей и числовые настройки.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = Constants.Config.INDICATORS_PREFIX)
public class IndicatorProperties {

    private Map<String, Integer> warmupBars = new HashMap<>();
    private Integer batchFlushBars;
    private Numeric numeric = new Numeric();

    public int getWarmupBars(String indicatorKey) {
        if (Objects.isNull(indicatorKey)) {
            return 0;
        }
        Integer value = warmupBars.get(indicatorKey);
        if (Objects.isNull(value)) {
            return 0;
        }
        return value;
    }

    @Getter
    @Setter
    public static class Numeric {

        private Integer priceScale;
        private RoundingMode mathRounding = RoundingMode.HALF_UP;
    }
}
