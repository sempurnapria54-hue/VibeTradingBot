package com.example.vibetradingbot.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO покрытия данных по таймфрейму.
 */
@Getter
@Builder
public class CoverageDto {

    private UUID exchangeInstrumentId;
    private String timeframe;
    private Instant coverageStartUtc;
    private Instant coverageEndUtc;
}
