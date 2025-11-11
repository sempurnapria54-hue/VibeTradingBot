package com.example.vibetradingbot.domain.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.example.vibetradingbot.domain.enums.Direction;
import com.example.vibetradingbot.domain.enums.QuorumAction;

import lombok.Builder;
import lombok.Getter;

/**
 * Структура объяснимости решения кворума.
 */
@Getter
@Builder
public class QuorumReason {

    private final List<Input> inputs;
    private final List<Filter> filters;
    private final Aggregation aggregation;
    private final QuorumAction action;
    private final Direction direction;
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
     * Вклад входных сигналов в итоговый скор кворума.
     */
    @Getter
    @Builder
    public static class Input {

        private final String type;
        private final String timeframe;
        private final BigDecimal score;
        private final BigDecimal weight;
        private final BigDecimal contribution;
        private final Map<String, String> fields;
    }

    /**
     * Информация о фильтрах (gatekeepers) кворума.
     */
    @Getter
    @Builder
    public static class Filter {

        private final String name;
        private final String mode;
        private final boolean passed;
        private final BigDecimal factor;
        private final Map<String, String> detail;
    }

    /**
     * Сводка агрегирования score и порогов.
     */
    @Getter
    @Builder
    public static class Aggregation {

        private final BigDecimal rawScore;
        private final BigDecimal afterFilters;
        private final Thresholds thresholds;
    }

    /**
     * Пороговые значения кворума.
     */
    @Getter
    @Builder
    public static class Thresholds {

        private final BigDecimal enter;
        private final BigDecimal exit;
    }
}
