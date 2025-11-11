package com.example.vibetradingbot.api.v1.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос запуска оффлайн-бэктеста.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestRunRequest {

    @NotNull
    private UUID exchangeInstrumentId;

    @NotBlank
    private String timeframe;

    @NotNull
    private Instant fromUtc;

    @NotNull
    private Instant toUtc;

    @NotEmpty
    private List<String> signalTypes;

    @NotNull
    private Long signalParamsId;

    private Long quorumParamsId;

    @NotNull
    private Long riskParamsId;

    @NotNull
    private Long exchangeParamsId;

    @NotNull
    private Long backtestParamsId;

    private JsonNode backtestParams;

    private Boolean allowDuplicate;
}
