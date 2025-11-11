package com.example.vibetradingbot.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Точка кривой капитала бэктеста.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class BacktestEquityPoint {

    private Long id;
    private Long runId;
    private Instant timestamp;
    private BigDecimal equityR;

    @Builder
    private BacktestEquityPoint(Long id, Long runId, Instant timestamp, BigDecimal equityR) {
        this.id = id;
        this.runId = Objects.requireNonNull(runId, Constants.Validation.NULL_IDENTIFIER);
        this.timestamp = Objects.requireNonNull(timestamp, Constants.Validation.INVALID_TIME_RANGE);
        this.equityR = equityR;
    }

    public static BacktestEquityPoint create(Long runId, Instant timestamp, BigDecimal equityR) {
        return BacktestEquityPoint.builder()
            .runId(runId)
            .timestamp(timestamp)
            .equityR(equityR)
            .build();
    }
}
