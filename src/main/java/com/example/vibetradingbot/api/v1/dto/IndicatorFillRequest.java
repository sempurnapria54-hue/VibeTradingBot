package com.example.vibetradingbot.api.v1.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос на расчёт значений индикатора.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorFillRequest {

    @NotNull
    private UUID exchangeInstrumentId;

    @NotBlank
    private String timeframe;

    @NotNull
    private Long paramsRef;
}
