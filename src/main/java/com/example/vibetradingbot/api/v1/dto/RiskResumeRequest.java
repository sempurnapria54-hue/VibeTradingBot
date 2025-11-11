package com.example.vibetradingbot.api.v1.dto;

import com.example.vibetradingbot.domain.enums.RiskHaltScope;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос на снятие HALT.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskResumeRequest {

    @NotNull
    private RiskHaltScope scope;

    private Long exchangeInstrumentId;
}
