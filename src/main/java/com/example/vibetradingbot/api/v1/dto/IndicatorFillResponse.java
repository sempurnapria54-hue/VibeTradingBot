package com.example.vibetradingbot.api.v1.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ на запуск расчёта индикатора.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorFillResponse {

    private UUID exchangeInstrumentId;
    private String timeframe;
    private Long paramsRef;
    private String indicator;
    private int insertedValues;
    private int warmupSkipped;
    private String message;
}
