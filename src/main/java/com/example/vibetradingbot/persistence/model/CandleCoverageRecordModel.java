package com.example.vibetradingbot.persistence.model;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

/**
 * Модель записи покрытия свечей.
 */
@Getter
@Setter
public class CandleCoverageRecordModel {

    /**
     * Уникальный идентификатор записи покрытия.
     */
    private UUID id;
    /**
     * Идентификатор связки биржи и инструмента.
     */
    private UUID exchangeInstrumentId;
    /**
     * Канонический таймфрейм.
     */
    private String timeframe;
    /**
     * Момент начала покрытия.
     */
    private Instant coverageStartUtc;
    /**
     * Момент завершения покрытия.
     */
    private Instant coverageEndUtc;
    /**
     * Флаг завершенности покрытия.
     */
    private Boolean complete;
    /**
     * Момент последнего обновления записи.
     */
    private Instant updatedAt;
}
