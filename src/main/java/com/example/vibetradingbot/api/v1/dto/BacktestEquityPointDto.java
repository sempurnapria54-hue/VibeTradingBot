package com.example.vibetradingbot.api.v1.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Точка кривой капитала в ответе API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestEquityPointDto {

    private Instant timestamp;
    private BigDecimal equityR;
}
