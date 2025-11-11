package com.example.vibetradingbot.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Интервал пропуска свечей.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class CandleGap {

    private Instant fromUtc;
    private Instant toUtc;

    @Builder
    private CandleGap(Instant fromUtc, Instant toUtc) {
        this.fromUtc = Objects.requireNonNull(fromUtc, Constants.Validation.INVALID_TIME_RANGE);
        this.toUtc = Objects.requireNonNull(toUtc, Constants.Validation.INVALID_TIME_RANGE);
    }

    public static CandleGap create(Instant fromUtc, Instant toUtc) {
        return CandleGap.builder()
            .fromUtc(fromUtc)
            .toUtc(toUtc)
            .build();
    }
}
