package com.example.vibetradingbot.persistence.model;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

/**
 * Модель записи инструмента для слоя persistence.
 */
@Getter
@Setter
public class InstrumentRecordModel {

    /**
     * Уникальный идентификатор инструмента.
     */
    private UUID id;
    /**
     * Канонический символ инструмента.
     */
    private String symbol;
    /**
     * Базовая валюта.
     */
    private String baseCurrency;
    /**
     * Котируемая валюта.
     */
    private String quoteCurrency;
    /**
     * Тип инструмента.
     */
    private String instrumentType;
    /**
     * Точность цены.
     */
    private Integer pricePrecision;
    /**
     * Точность количества.
     */
    private Integer quantityPrecision;
    /**
     * Момент создания записи.
     */
    private Instant createdAt;
    /**
     * Момент последнего обновления.
     */
    private Instant updatedAt;
}
