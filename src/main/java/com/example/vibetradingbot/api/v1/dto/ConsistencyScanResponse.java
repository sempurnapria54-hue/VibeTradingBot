package com.example.vibetradingbot.api.v1.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

/**
 * Ответ проверки целостности.
 */
@Getter
@Builder
public class ConsistencyScanResponse {

    private UUID exchangeInstrumentId;
    private String timeframe;
    private List<ConsistencyGapDto> gaps;
    private long missingBars;
    private long misalignedCount;
    private Instant lastCandleUtc;
    private String message;
}
