package com.example.vibetradingbot.api.v1.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO сделки бэктеста.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestTradeDto {

    private Long id;
    private Long signalId;
    private Instant entryTs;
    private Instant exitTs;
    private String side;
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;
    private BigDecimal pnlR;
    private BigDecimal fees;
    private BigDecimal slippage;
    private BigDecimal funding;
    private BigDecimal maeR;
    private BigDecimal mfeR;
    private String exitReason;
    private Integer timeToEventBars;
    private Object reason;
}
