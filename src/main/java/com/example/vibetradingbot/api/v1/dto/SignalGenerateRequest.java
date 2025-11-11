package com.example.vibetradingbot.api.v1.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос на инкрементальную генерацию сигналов.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalGenerateRequest {

    @NotNull
    private UUID exchangeInstrumentId;

    @NotBlank
    private String timeframe;

    @NotBlank
    private String signalType;

    @NotNull
    private Long paramsRef;
}
