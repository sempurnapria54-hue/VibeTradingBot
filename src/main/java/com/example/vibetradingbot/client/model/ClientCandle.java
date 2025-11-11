package com.example.vibetradingbot.client.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

/**
 * Модель свечи, получаемой от внешней биржи.
 */
@Value
@Builder
public class ClientCandle {

    UUID externalId;
    UUID exchangeInstrumentId;
    String timeframe;
    Instant openTime;
    Instant closeTime;
    BigDecimal openPrice;
    BigDecimal closePrice;
    BigDecimal highPrice;
    BigDecimal lowPrice;
    BigDecimal volume;
    long tradesCount;
}
