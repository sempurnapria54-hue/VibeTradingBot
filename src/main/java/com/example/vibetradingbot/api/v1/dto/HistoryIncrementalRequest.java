package com.example.vibetradingbot.api.v1.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

/**
 * Запрос инкрементальной докачки истории.
 */
@Getter
@Setter
public class HistoryIncrementalRequest {

    @NotNull
    private UUID exchangeInstrumentId;

    @NotBlank
    private String timeframe;
}
