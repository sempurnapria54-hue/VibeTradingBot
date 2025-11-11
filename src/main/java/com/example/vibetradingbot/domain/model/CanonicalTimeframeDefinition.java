package com.example.vibetradingbot.domain.model;

import java.util.Map;
import java.util.Objects;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.util.Constants;

import lombok.Getter;

/**
 * Описание канонического таймфрейма и его соответствий на биржах.
 */
@Getter
public class CanonicalTimeframeDefinition {

    private final CanonicalTimeframe timeframe;
    private final int minutes;
    private final Map<String, String> exchangeMappings;

    public CanonicalTimeframeDefinition(CanonicalTimeframe timeframe, int minutes, Map<String, String> exchangeMappings) {
        this.timeframe = Objects.requireNonNull(timeframe, Constants.Validation.NULL_TIMEFRAME);
        this.minutes = minutes;
        this.exchangeMappings = Objects.requireNonNull(exchangeMappings, Constants.Validation.NULL_IDENTIFIER);
    }
}
