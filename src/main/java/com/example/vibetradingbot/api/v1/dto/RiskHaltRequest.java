package com.example.vibetradingbot.api.v1.dto;

import com.example.vibetradingbot.domain.enums.RiskHaltScope;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос на постановку HALT.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskHaltRequest {

    @NotNull
    private RiskHaltScope scope;

    private Long exchangeInstrumentId;

    @NotBlank
    private String reason;
}
