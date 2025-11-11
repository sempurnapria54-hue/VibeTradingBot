package com.example.vibetradingbot.api.v1.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO агрегированных метрик бэктеста.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestPerformanceDto {

    private Integer trades;
    private BigDecimal winrate;
    private BigDecimal profitFactor;
    private BigDecimal expectancyR;
    private BigDecimal maxDrawdownR;
    private BigDecimal sharpe;
    private BigDecimal sortino;
}
