package com.example.vibetradingbot.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Результат операций загрузки истории.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class HistoryOperationResult {

    private UUID exchangeInstrumentId;
    private CanonicalTimeframe timeframe;
    private long insertedBars;
    private Instant coverageStart;
    private Instant coverageEnd;

    @Builder
    private HistoryOperationResult(UUID exchangeInstrumentId, CanonicalTimeframe timeframe, long insertedBars,
            Instant coverageStart, Instant coverageEnd) {
        this.exchangeInstrumentId = Objects.requireNonNull(exchangeInstrumentId, Constants.Validation.NULL_IDENTIFIER);
        this.timeframe = Objects.requireNonNull(timeframe, Constants.Validation.NULL_TIMEFRAME);
        this.insertedBars = insertedBars;
        this.coverageStart = coverageStart;
        this.coverageEnd = coverageEnd;
    }

    public static HistoryOperationResult create(UUID exchangeInstrumentId, CanonicalTimeframe timeframe,
            long insertedBars, Instant coverageStart, Instant coverageEnd) {
        return HistoryOperationResult.builder()
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .insertedBars(insertedBars)
            .coverageStart(coverageStart)
            .coverageEnd(coverageEnd)
            .build();
    }
}
