package com.example.vibetradingbot.api.v1.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

/**
 * Запрос на инкрементальное выполнение кворума.
 */
@Getter
@Setter
public class QuorumDecideRequest {

    @NotNull
    private UUID exchangeInstrumentId;

    @NotNull
    private String timeframe;

    @NotNull
    private Long quorumParamsRef;
}
