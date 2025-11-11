package com.example.vibetradingbot.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.example.vibetradingbot.domain.enums.InstrumentType;
import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Доменная модель инструмента.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class Instrument {

    private UUID id;
    private String symbol;
    private String baseCurrency;
    private String quoteCurrency;
    private InstrumentType instrumentType;
    private int pricePrecision;
    private int quantityPrecision;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder
    private Instrument(UUID id, String symbol, String baseCurrency, String quoteCurrency, InstrumentType instrumentType,
            int pricePrecision, int quantityPrecision, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, Constants.Validation.NULL_IDENTIFIER);
        this.symbol = Objects.requireNonNull(symbol, Constants.Validation.NULL_IDENTIFIER);
        this.baseCurrency = Objects.requireNonNull(baseCurrency, Constants.Validation.NULL_IDENTIFIER);
        this.quoteCurrency = Objects.requireNonNull(quoteCurrency, Constants.Validation.NULL_IDENTIFIER);
        this.instrumentType = Objects.requireNonNull(instrumentType, Constants.Validation.NULL_IDENTIFIER);
        this.pricePrecision = pricePrecision;
        this.quantityPrecision = quantityPrecision;
        this.createdAt = Objects.requireNonNull(createdAt, Constants.Validation.NULL_IDENTIFIER);
        this.updatedAt = Objects.requireNonNull(updatedAt, Constants.Validation.NULL_IDENTIFIER);
    }

    public static Instrument create(UUID id, String symbol, String baseCurrency, String quoteCurrency,
            InstrumentType instrumentType, int pricePrecision, int quantityPrecision, Instant createdAt,
            Instant updatedAt) {
        return Instrument.builder()
            .id(id)
            .symbol(symbol)
            .baseCurrency(baseCurrency)
            .quoteCurrency(quoteCurrency)
            .instrumentType(instrumentType)
            .pricePrecision(pricePrecision)
            .quantityPrecision(quantityPrecision)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }
}
