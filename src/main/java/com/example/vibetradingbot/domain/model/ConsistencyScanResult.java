package com.example.vibetradingbot.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Результат проверки целостности серии свечей.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class ConsistencyScanResult {

    private UUID exchangeInstrumentId;
    private CanonicalTimeframe timeframe;
    private List<CandleGap> gaps;
    private long missingBars;
    private long misalignedCount;
    private Instant lastCandleUtc;

    @Builder
    private ConsistencyScanResult(UUID exchangeInstrumentId, CanonicalTimeframe timeframe, List<CandleGap> gaps,
            long missingBars, long misalignedCount, Instant lastCandleUtc) {
        this.exchangeInstrumentId = Objects.requireNonNull(exchangeInstrumentId, Constants.Validation.NULL_IDENTIFIER);
        this.timeframe = Objects.requireNonNull(timeframe, Constants.Validation.NULL_TIMEFRAME);
        this.gaps = gaps == null ? Collections.emptyList() : gaps;
        this.missingBars = missingBars;
        this.misalignedCount = misalignedCount;
        this.lastCandleUtc = lastCandleUtc;
    }

    public static ConsistencyScanResult create(UUID exchangeInstrumentId, CanonicalTimeframe timeframe,
            List<CandleGap> gaps, long missingBars, long misalignedCount, Instant lastCandleUtc) {
        return ConsistencyScanResult.builder()
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .gaps(gaps)
            .missingBars(missingBars)
            .misalignedCount(misalignedCount)
            .lastCandleUtc(lastCandleUtc)
            .build();
    }
}
