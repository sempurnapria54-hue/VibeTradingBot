package com.example.vibetradingbot.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос на генерацию сигналов в заданном диапазоне времени.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalGenerateRangeRequest {

    @NotNull
    private UUID exchangeInstrumentId;

    @NotBlank
    private String timeframe;

    @NotBlank
    private String signalType;

    @NotNull
    private Long paramsRef;

    @NotNull
    private Instant fromUtc;

    @NotNull
    private Instant toUtc;
}
