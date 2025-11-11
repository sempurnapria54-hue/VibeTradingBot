package com.example.vibetradingbot.api.v1.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO позиции.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LivePositionDto {

    private Long exchangeInstrumentId;
    private BigDecimal size;
    private BigDecimal averagePrice;
    private BigDecimal stopLoss;
    private BigDecimal takeProfit;
    private BigDecimal unrealizedPnl;
    private int leverage;
    private boolean isolated;
    private long stateVersion;
    private Instant updatedAt;
}
