package com.example.vibetradingbot.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

/**
 * Запрос целевой докачки диапазона.
 */
@Getter
@Setter
public class HistoryRangeRequest {

    @NotNull
    private UUID exchangeInstrumentId;

    @NotBlank
    private String timeframe;

    @NotNull
    private Instant fromUtc;

    @NotNull
    private Instant toUtc;
}
