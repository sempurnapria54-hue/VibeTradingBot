package com.example.vibetradingbot.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Доменная модель связки биржи и инструмента.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class ExchangeInstrument {

    private UUID id;
    private UUID exchangeId;
    private UUID instrumentId;
    private String exchangeSymbol;
    private BigDecimal minTradeQuantity;
    private BigDecimal tickSize;
    private BigDecimal makerFeeRate;
    private BigDecimal takerFeeRate;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder
    private ExchangeInstrument(UUID id, UUID exchangeId, UUID instrumentId, String exchangeSymbol,
            BigDecimal minTradeQuantity, BigDecimal tickSize, BigDecimal makerFeeRate, BigDecimal takerFeeRate,
            boolean active, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, Constants.Validation.NULL_IDENTIFIER);
        this.exchangeId = Objects.requireNonNull(exchangeId, Constants.Validation.NULL_IDENTIFIER);
        this.instrumentId = Objects.requireNonNull(instrumentId, Constants.Validation.NULL_IDENTIFIER);
        this.exchangeSymbol = Objects.requireNonNull(exchangeSymbol, Constants.Validation.NULL_IDENTIFIER);
        this.minTradeQuantity = Objects.requireNonNull(minTradeQuantity, Constants.Validation.NULL_IDENTIFIER);
        this.tickSize = Objects.requireNonNull(tickSize, Constants.Validation.NULL_IDENTIFIER);
        this.makerFeeRate = Objects.requireNonNull(makerFeeRate, Constants.Validation.NULL_IDENTIFIER);
        this.takerFeeRate = Objects.requireNonNull(takerFeeRate, Constants.Validation.NULL_IDENTIFIER);
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt, Constants.Validation.NULL_IDENTIFIER);
        this.updatedAt = Objects.requireNonNull(updatedAt, Constants.Validation.NULL_IDENTIFIER);
    }

    public static ExchangeInstrument create(UUID id, UUID exchangeId, UUID instrumentId, String exchangeSymbol,
            BigDecimal minTradeQuantity, BigDecimal tickSize, BigDecimal makerFeeRate, BigDecimal takerFeeRate,
            boolean active, Instant createdAt, Instant updatedAt) {
        return ExchangeInstrument.builder()
            .id(id)
            .exchangeId(exchangeId)
            .instrumentId(instrumentId)
            .exchangeSymbol(exchangeSymbol)
            .minTradeQuantity(minTradeQuantity)
            .tickSize(tickSize)
            .makerFeeRate(makerFeeRate)
            .takerFeeRate(takerFeeRate)
            .active(active)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }
}
