package com.example.vibetradingbot.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ по текущему чекпоинту индикатора.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorCheckpointResponse {

    private String indicator;
    private String timeframe;
    private UUID exchangeInstrumentId;
    private Long paramsRef;
    private Integer version;
    private Instant lastTimestamp;
    private Instant updatedAt;
}
