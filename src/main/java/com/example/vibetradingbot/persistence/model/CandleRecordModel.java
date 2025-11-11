package com.example.vibetradingbot.persistence.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

/**
 * Модель записи свечи для слоя persistence.
 */
@Getter
@Setter
public class CandleRecordModel {

    /**
     * Уникальный идентификатор свечи.
     */
    private UUID id;
    /**
     * Идентификатор связки биржи и инструмента.
     */
    private UUID exchangeInstrumentId;
    /**
     * Канонический таймфрейм свечи.
     */
    private String timeframe;
    /**
     * Время открытия свечи в UTC.
     */
    private Instant openTimeUtc;
    /**
     * Время закрытия свечи в UTC.
     */
    private Instant closeTimeUtc;
    /**
     * Цена открытия свечи.
     */
    private BigDecimal openPrice;
    /**
     * Цена закрытия свечи.
     */
    private BigDecimal closePrice;
    /**
     * Максимальная цена свечи.
     */
    private BigDecimal highPrice;
    /**
     * Минимальная цена свечи.
     */
    private BigDecimal lowPrice;
    /**
     * Торговый объем свечи.
     */
    private BigDecimal volume;
    /**
     * Количество сделок в свечe.
     */
    private Long tradesCount;
    /**
     * Момент покрытия данных.
     */
    private Instant coverageEndUtc;
}
