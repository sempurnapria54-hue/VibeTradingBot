package com.example.vibetradingbot.persistence.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

/**
 * Модель связки биржи и инструмента для слоя persistence.
 */
@Getter
@Setter
public class ExchangeInstrumentRecordModel {

    /**
     * Уникальный идентификатор связки.
     */
    private UUID id;
    /**
     * Идентификатор биржи.
     */
    private UUID exchangeId;
    /**
     * Идентификатор инструмента.
     */
    private UUID instrumentId;
    /**
     * Символ на бирже.
     */
    private String exchangeSymbol;
    /**
     * Минимальное количество сделки.
     */
    private BigDecimal minTradeQuantity;
    /**
     * Шаг цены.
     */
    private BigDecimal tickSize;
    /**
     * Комиссия мейкера.
     */
    private BigDecimal makerFeeRate;
    /**
     * Комиссия тейкера.
     */
    private BigDecimal takerFeeRate;
    /**
     * Флаг активности.
     */
    private Boolean active;
    /**
     * Момент создания.
     */
    private Instant createdAt;
    /**
     * Момент обновления.
     */
    private Instant updatedAt;
}
