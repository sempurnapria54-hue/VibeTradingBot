package com.example.tradingbot.domain.model;

import com.example.tradingbot.domain.value.Quantity;
import com.example.tradingbot.domain.value.TimestampUtc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

public final class Candle {

    private final HistoryGroup historyGroup;
    private final TimestampUtc timestampUtc;
    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;
    private final Quantity volumeCoin;
    private final Quantity volumeCurrency;

    private Candle(
        HistoryGroup historyGroup,
        TimestampUtc timestampUtc,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        Quantity volumeCoin,
        Quantity volumeCurrency
    ) {
        this.historyGroup = historyGroup;
        this.timestampUtc = timestampUtc;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volumeCoin = volumeCoin;
        this.volumeCurrency = volumeCurrency;
    }

    public static Candle create(
        HistoryGroup historyGroup,
        Instant timestamp,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal volumeCoin,
        BigDecimal volumeCurrency
    ) {
        Objects.requireNonNull(historyGroup, "historyGroup");
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(open, "open");
        Objects.requireNonNull(high, "high");
        Objects.requireNonNull(low, "low");
        Objects.requireNonNull(close, "close");
        ensurePrice(open, "open");
        ensurePrice(high, "high");
        ensurePrice(low, "low");
        ensurePrice(close, "close");
        if (high.compareTo(low) < 0) {
            throw new IllegalArgumentException("high must be >= low");
        }
        Quantity volCoin = volumeCoin != null ? Quantity.of(volumeCoin) : null;
        Quantity volCurrency = volumeCurrency != null ? Quantity.of(volumeCurrency) : null;
        return new Candle(
            historyGroup,
            TimestampUtc.of(timestamp),
            open,
            high,
            low,
            close,
            volCoin,
            volCurrency
        );
    }

    private static void ensurePrice(BigDecimal value, String field) {
        if (value.signum() < 0) {
            throw new IllegalArgumentException(field + " price cannot be negative");
        }
    }

    public static Candle rehydrate(
        HistoryGroup historyGroup,
        Instant timestamp,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        Quantity volumeCoin,
        Quantity volumeCurrency
    ) {
        Objects.requireNonNull(historyGroup, "historyGroup");
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(open, "open");
        Objects.requireNonNull(high, "high");
        Objects.requireNonNull(low, "low");
        Objects.requireNonNull(close, "close");
        return new Candle(
            historyGroup,
            TimestampUtc.of(timestamp),
            open,
            high,
            low,
            close,
            volumeCoin,
            volumeCurrency
        );
    }

    public HistoryGroup historyGroup() {
        return historyGroup;
    }

    public TimestampUtc timestampUtc() {
        return timestampUtc;
    }

    public BigDecimal open() {
        return open;
    }

    public BigDecimal high() {
        return high;
    }

    public BigDecimal low() {
        return low;
    }

    public BigDecimal close() {
        return close;
    }

    public Quantity volumeCoin() {
        return volumeCoin;
    }

    public Quantity volumeCurrency() {
        return volumeCurrency;
    }

    public BigDecimal mid() {
        return high.add(low).divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP);
    }
}
