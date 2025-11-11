package com.example.vibetradingbot.domain.model;

import java.util.Objects;
import java.util.UUID;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.util.Constants;

import lombok.Getter;

/**
 * Команда онбординга новой связки биржи и инструмента.
 */
@Getter
public class OnboardingCommand {

    private final UUID exchangeId;
    private final UUID instrumentId;
    private final CanonicalTimeframe timeframe;

    public OnboardingCommand(UUID exchangeId, UUID instrumentId, CanonicalTimeframe timeframe) {
        this.exchangeId = Objects.requireNonNull(exchangeId, Constants.Validation.NULL_IDENTIFIER);
        this.instrumentId = Objects.requireNonNull(instrumentId, Constants.Validation.NULL_IDENTIFIER);
        this.timeframe = Objects.requireNonNull(timeframe, Constants.Validation.NULL_TIMEFRAME);
    }
}
