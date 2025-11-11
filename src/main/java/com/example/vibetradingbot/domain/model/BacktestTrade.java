package com.example.vibetradingbot.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import com.example.vibetradingbot.domain.enums.BacktestSide;
import com.example.vibetradingbot.domain.enums.ExitReason;
import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Сделка бэктеста с основными метриками исполнения.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class BacktestTrade {

    private Long id;
    private Long runId;
    private Long signalId;
    private Instant entryTimestamp;
    private Instant exitTimestamp;
    private BacktestSide side;
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;
    private BigDecimal pnlR;
    private BigDecimal fees;
    private BigDecimal slippage;
    private BigDecimal funding;
    private BigDecimal maeR;
    private BigDecimal mfeR;
    private ExitReason exitReason;
    private Integer timeToEventBars;
    private String reason;

    @Builder
    private BacktestTrade(Long id, Long runId, Long signalId, Instant entryTimestamp, Instant exitTimestamp,
            BacktestSide side, BigDecimal entryPrice, BigDecimal exitPrice, BigDecimal pnlR, BigDecimal fees,
            BigDecimal slippage, BigDecimal funding, BigDecimal maeR, BigDecimal mfeR, ExitReason exitReason,
            Integer timeToEventBars, String reason) {
        this.id = id;
        this.runId = Objects.requireNonNull(runId, Constants.Validation.NULL_IDENTIFIER);
        this.signalId = signalId;
        this.entryTimestamp = Objects.requireNonNull(entryTimestamp, Constants.Validation.INVALID_TIME_RANGE);
        this.exitTimestamp = Objects.requireNonNull(exitTimestamp, Constants.Validation.INVALID_TIME_RANGE);
        this.side = side;
        this.entryPrice = entryPrice;
        this.exitPrice = exitPrice;
        this.pnlR = pnlR;
        this.fees = fees;
        this.slippage = slippage;
        this.funding = funding;
        this.maeR = maeR;
        this.mfeR = mfeR;
        this.exitReason = exitReason;
        this.timeToEventBars = timeToEventBars;
        this.reason = reason;
    }

    public static BacktestTrade create(Long runId, Long signalId, Instant entryTimestamp, Instant exitTimestamp,
            BacktestSide side, BigDecimal entryPrice, BigDecimal exitPrice, BigDecimal pnlR, BigDecimal fees,
            BigDecimal slippage, BigDecimal funding, BigDecimal maeR, BigDecimal mfeR, ExitReason exitReason,
            Integer timeToEventBars, String reason) {
        return BacktestTrade.builder()
            .runId(runId)
            .signalId(signalId)
            .entryTimestamp(entryTimestamp)
            .exitTimestamp(exitTimestamp)
            .side(side)
            .entryPrice(entryPrice)
            .exitPrice(exitPrice)
            .pnlR(pnlR)
            .fees(fees)
            .slippage(slippage)
            .funding(funding)
            .maeR(maeR)
            .mfeR(mfeR)
            .exitReason(exitReason)
            .timeToEventBars(timeToEventBars)
            .reason(reason)
            .build();
    }
}
