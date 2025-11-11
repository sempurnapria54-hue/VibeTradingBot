package com.example.vibetradingbot.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.exception.ServiceException;
import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Доменная модель покрытия данных.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class CandleCoverage {

    private UUID id;
    private UUID exchangeInstrumentId;
    private CanonicalTimeframe timeframe;
    private Instant coverageStart;
    private Instant coverageEnd;
    private boolean complete;
    private Instant updatedAt;

    @Builder
    private CandleCoverage(UUID id, UUID exchangeInstrumentId, CanonicalTimeframe timeframe, Instant coverageStart,
            Instant coverageEnd, boolean complete, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, Constants.Validation.NULL_IDENTIFIER);
        this.exchangeInstrumentId = Objects.requireNonNull(exchangeInstrumentId, Constants.Validation.NULL_IDENTIFIER);
        this.timeframe = Objects.requireNonNull(timeframe, Constants.Validation.NULL_TIMEFRAME);
        this.coverageStart = Objects.requireNonNull(coverageStart, Constants.Validation.INVALID_TIME_RANGE);
        this.coverageEnd = Objects.requireNonNull(coverageEnd, Constants.Validation.INVALID_TIME_RANGE);
        this.complete = complete;
        this.updatedAt = Objects.requireNonNull(updatedAt, Constants.Validation.INVALID_TIME_RANGE);
        validateChronology();
    }

    public static CandleCoverage create(UUID id, UUID exchangeInstrumentId, CanonicalTimeframe timeframe,
            Instant coverageStart, Instant coverageEnd, boolean complete, Instant updatedAt) {
        return CandleCoverage.builder()
            .id(id)
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .coverageStart(coverageStart)
            .coverageEnd(coverageEnd)
            .complete(complete)
            .updatedAt(updatedAt)
            .build();
    }

    public boolean shouldRequestBackfill(Instant expectedEnd) {
        if (BooleanUtils.isFalse(complete)) {
            return coverageEnd.isBefore(expectedEnd);
        }
        return false;
    }

    private void validateChronology() {
        if (!coverageEnd.isAfter(coverageStart)) {
            throw new ServiceException(Constants.Errors.DOMAIN_VALIDATION_FAILED, Constants.Validation.INVALID_TIME_RANGE);
        }
    }
}
