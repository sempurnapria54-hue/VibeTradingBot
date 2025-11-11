package com.example.vibetradingbot.api.v1.dto;

import java.time.Instant;

import com.example.vibetradingbot.domain.enums.RiskHaltScope;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO флага остановки торговли.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskHaltStateDto {

    private RiskHaltScope scope;
    private Long exchangeInstrumentId;
    private boolean halted;
    private String reason;
    private Instant updatedAt;
}
