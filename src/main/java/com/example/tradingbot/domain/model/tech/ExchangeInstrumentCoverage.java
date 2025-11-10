package com.example.tradingbot.domain.model.tech;

import com.example.tradingbot.domain.model.ExchangeInstrument;
import com.example.tradingbot.domain.value.TimestampUtc;

import java.time.Instant;
import java.util.Objects;

public final class ExchangeInstrumentCoverage {

    private final ExchangeInstrument exchangeInstrument;
    private final TimestampUtc coverageStart;
    private final TimestampUtc coverageEnd;

    private ExchangeInstrumentCoverage(
        ExchangeInstrument exchangeInstrument,
        TimestampUtc coverageStart,
        TimestampUtc coverageEnd
    ) {
        this.exchangeInstrument = exchangeInstrument;
        this.coverageStart = coverageStart;
        this.coverageEnd = coverageEnd;
    }

    public static ExchangeInstrumentCoverage create(
        ExchangeInstrument exchangeInstrument,
        Instant coverageStart,
        Instant coverageEnd
    ) {
        Objects.requireNonNull(exchangeInstrument, "exchangeInstrument");
        Objects.requireNonNull(coverageStart, "coverageStart");
        TimestampUtc start = TimestampUtc.of(coverageStart);
        TimestampUtc end = coverageEnd != null ? TimestampUtc.of(coverageEnd) : null;
        return new ExchangeInstrumentCoverage(exchangeInstrument, start, end);
    }

    public static ExchangeInstrumentCoverage rehydrate(
        ExchangeInstrument exchangeInstrument,
        Instant coverageStart,
        Instant coverageEnd
    ) {
        return create(exchangeInstrument, coverageStart, coverageEnd);
    }

    public ExchangeInstrument exchangeInstrument() {
        return exchangeInstrument;
    }

    public TimestampUtc coverageStart() {
        return coverageStart;
    }

    public TimestampUtc coverageEnd() {
        return coverageEnd;
    }
}
