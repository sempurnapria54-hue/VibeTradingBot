package com.example.vibetradingbot.domain.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.example.vibetradingbot.domain.enums.SignalDirection;

import lombok.Builder;
import lombok.Getter;

/**
 * Объяснимость сигнала (inputs, фильтры, итоговый скор и заметки).
 */
@Getter
@Builder
public class SignalReason {

    private final List<Input> inputs;
    private final List<Filter> filters;
    private final BigDecimal score;
    private final SignalDirection direction;
    private final String notes;

    public List<Input> getInputs() {
        if (Objects.isNull(inputs)) {
            return List.of();
        }
        return Collections.unmodifiableList(inputs);
    }

    public List<Filter> getFilters() {
        if (Objects.isNull(filters)) {
            return List.of();
        }
        return Collections.unmodifiableList(filters);
    }

    /**
     * Описание входа (индикатора) в итоговом сигнале.
     */
    @Getter
    @Builder
    public static class Input {

        private final String indicator;
        private final String timeframe;
        private final Map<String, String> fields;
        private final BigDecimal weight;
        private final BigDecimal contribution;
    }

    /**
     * Описание фильтров (gatekeepers) сигнала.
     */
    @Getter
    @Builder
    public static class Filter {

        private final String name;
        private final boolean passed;
        private final Map<String, String> detail;
    }
}
