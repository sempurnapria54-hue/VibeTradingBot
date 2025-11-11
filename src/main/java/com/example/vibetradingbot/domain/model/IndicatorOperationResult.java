package com.example.vibetradingbot.domain.model;

import java.util.UUID;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.enums.IndicatorType;

import lombok.Builder;
import lombok.Getter;

/**
 * Результат расчёта индикатора по серии.
 */
@Getter
@Builder
public class IndicatorOperationResult {

    private final UUID exchangeInstrumentId;
    private final CanonicalTimeframe timeframe;
    private final IndicatorType indicator;
    private final long paramsRef;
    private final int insertedValues;
    private final int warmupSkipped;
}
