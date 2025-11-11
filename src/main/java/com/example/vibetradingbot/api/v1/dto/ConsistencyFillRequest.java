package com.example.vibetradingbot.api.v1.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

/**
 * Запрос на докачку пропусков.
 */
@Getter
@Setter
public class ConsistencyFillRequest {

    @NotNull
    private UUID exchangeInstrumentId;

    @NotBlank
    private String timeframe;

    private List<ConsistencyGapDto> gaps;
}
