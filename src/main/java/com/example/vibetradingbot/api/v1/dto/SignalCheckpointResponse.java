package com.example.vibetradingbot.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ с чекпоинтом генерации сигналов.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalCheckpointResponse {

    private String signalType;
    private String timeframe;
    private UUID exchangeInstrumentId;
    private Long paramsRef;
    private Integer version;
    private Instant lastTimestamp;
    private Instant updatedAt;
}
