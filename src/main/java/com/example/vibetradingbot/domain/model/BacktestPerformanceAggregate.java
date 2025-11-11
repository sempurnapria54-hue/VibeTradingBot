package com.example.vibetradingbot.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Агрегированные показатели эффективности запуска бэктеста.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class BacktestPerformanceAggregate {

    private Long runId;
    private Integer trades;
    private BigDecimal winrate;
    private BigDecimal profitFactor;
    private BigDecimal expectancyR;
    private BigDecimal maxDrawdownR;
    private BigDecimal sharpe;
    private BigDecimal sortino;

    @Builder
    private BacktestPerformanceAggregate(Long runId, Integer trades, BigDecimal winrate, BigDecimal profitFactor,
            BigDecimal expectancyR, BigDecimal maxDrawdownR, BigDecimal sharpe, BigDecimal sortino) {
        this.runId = Objects.requireNonNull(runId, Constants.Validation.NULL_IDENTIFIER);
        this.trades = trades;
        this.winrate = winrate;
        this.profitFactor = profitFactor;
        this.expectancyR = expectancyR;
        this.maxDrawdownR = maxDrawdownR;
        this.sharpe = sharpe;
        this.sortino = sortino;
    }

    public static BacktestPerformanceAggregate create(Long runId, Integer trades, BigDecimal winrate,
            BigDecimal profitFactor, BigDecimal expectancyR, BigDecimal maxDrawdownR, BigDecimal sharpe,
            BigDecimal sortino) {
        return BacktestPerformanceAggregate.builder()
            .runId(runId)
            .trades(trades)
            .winrate(winrate)
            .profitFactor(profitFactor)
            .expectancyR(expectancyR)
            .maxDrawdownR(maxDrawdownR)
            .sharpe(sharpe)
            .sortino(sortino)
            .build();
    }
}
