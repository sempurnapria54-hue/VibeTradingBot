package com.example.vibetradingbot.api.v1.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ на запуск генерации сигналов.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalOperationResponse {

    private UUID exchangeInstrumentId;
    private String timeframe;
    private String signalType;
    private Long paramsRef;
    private Integer insertedSignals;
    private Integer skippedByDebounce;
    private Integer skippedByCooldown;
    private String message;
}
