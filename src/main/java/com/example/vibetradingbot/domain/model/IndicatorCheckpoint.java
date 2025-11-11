package com.example.vibetradingbot.domain.model;

import java.time.Instant;
import java.util.UUID;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.enums.IndicatorType;

import lombok.Builder;
import lombok.Getter;

/**
 * Доменная модель чекпоинта индикатора.
 */
@Getter
@Builder
public class IndicatorCheckpoint {

    private final IndicatorType indicator;
    private final CanonicalTimeframe timeframe;
    private final UUID exchangeInstrumentId;
    private final long paramsRef;
    private final int version;
    private final Instant lastTimestamp;
    private final Instant updatedAt;
}
