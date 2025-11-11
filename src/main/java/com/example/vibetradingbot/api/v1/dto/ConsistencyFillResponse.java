package com.example.vibetradingbot.api.v1.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

/**
 * Ответ на докачку пропусков.
 */
@Getter
@Builder
public class ConsistencyFillResponse {

    private UUID exchangeInstrumentId;
    private String timeframe;
    private int processedGaps;
    private String message;
}
