package com.example.vibetradingbot.persistence.model;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

/**
 * Модель записи биржи для слоя persistence.
 */
@Getter
@Setter
public class ExchangeRecordModel {

    /**
     * Уникальный идентификатор биржи.
     */
    private UUID id;
    /**
     * Канонический код биржи.
     */
    private String code;
    /**
     * Название биржи.
     */
    private String name;
    /**
     * Статус биржи.
     */
    private String status;
    /**
     * Момент создания записи.
     */
    private Instant createdAt;
    /**
     * Момент последнего обновления.
     */
    private Instant updatedAt;
}
