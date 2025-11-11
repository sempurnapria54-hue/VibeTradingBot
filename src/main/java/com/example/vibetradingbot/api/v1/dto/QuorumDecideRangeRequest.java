package com.example.vibetradingbot.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

/**
 * Запрос на пакетное выполнение кворума в диапазоне.
 */
@Getter
@Setter
public class QuorumDecideRangeRequest {

    @NotNull
    private UUID exchangeInstrumentId;

    @NotNull
    private String timeframe;

    @NotNull
    private Long quorumParamsRef;

    @NotNull
    private Instant fromUtc;

    @NotNull
    private Instant toUtc;
}
