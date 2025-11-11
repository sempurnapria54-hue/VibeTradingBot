package com.example.vibetradingbot.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

/**
 * Ответ операций загрузки истории.
 */
@Getter
@Builder
public class HistoryOperationResponse {

    private UUID exchangeInstrumentId;
    private String timeframe;
    private long insertedBars;
    private Instant coverageStartUtc;
    private Instant coverageEndUtc;
    private String message;
}
