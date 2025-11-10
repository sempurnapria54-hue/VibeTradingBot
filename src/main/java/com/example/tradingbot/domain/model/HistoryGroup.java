package com.example.tradingbot.domain.model;

import com.example.tradingbot.domain.value.Timeframe;
import com.example.tradingbot.domain.value.TimestampUtc;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class HistoryGroup {

    private final Long id;
    private final ExchangeInstrument exchangeInstrument;
    private final Timeframe timeframe;
    private final TimestampUtc coverageStartUtc;

    private HistoryGroup(
        Long id,
        ExchangeInstrument exchangeInstrument,
        Timeframe timeframe,
        TimestampUtc coverageStartUtc
    ) {
        this.id = id;
        this.exchangeInstrument = exchangeInstrument;
        this.timeframe = timeframe;
        this.coverageStartUtc = coverageStartUtc;
    }

    public static HistoryGroup create(
        ExchangeInstrument exchangeInstrument,
        String timeframeCanonical,
        Set<String> allowedTimeframes,
        Instant coverageStart
    ) {
        Objects.requireNonNull(exchangeInstrument, "exchangeInstrument");
        Objects.requireNonNull(timeframeCanonical, "timeframeCanonical");
        Objects.requireNonNull(allowedTimeframes, "allowedTimeframes");
        Timeframe timeframe = Timeframe.of(timeframeCanonical, allowedTimeframes);
        TimestampUtc coverage = coverageStart != null ? TimestampUtc.of(coverageStart) : null;
        return new HistoryGroup(null, exchangeInstrument, timeframe, coverage);
    }

    public HistoryGroup withId(Long newId) {
        Objects.requireNonNull(newId, "newId");
        return new HistoryGroup(newId, exchangeInstrument, timeframe, coverageStartUtc);
    }

    public static HistoryGroup rehydrate(
        Long id,
        ExchangeInstrument exchangeInstrument,
        Timeframe timeframe,
        TimestampUtc coverageStartUtc
    ) {
        Objects.requireNonNull(id, "id");
        return new HistoryGroup(id, exchangeInstrument, timeframe, coverageStartUtc);
    }

    public Long id() {
        return id;
    }

    public ExchangeInstrument exchangeInstrument() {
        return exchangeInstrument;
    }

    public Timeframe timeframe() {
        return timeframe;
    }

    public TimestampUtc coverageStartUtc() {
        return coverageStartUtc;
    }
}
