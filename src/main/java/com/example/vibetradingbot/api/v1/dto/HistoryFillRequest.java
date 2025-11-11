package com.example.vibetradingbot.api.v1.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

/**
 * Запрос первичной загрузки истории.
 */
@Getter
@Setter
public class HistoryFillRequest {

    @NotNull
    private UUID exchangeInstrumentId;

    private List<String> timeframes;
}
