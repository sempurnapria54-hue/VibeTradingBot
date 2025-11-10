package com.example.tradingbot.domain.model.tech;

import com.example.tradingbot.domain.model.HistoryGroup;
import com.example.tradingbot.domain.model.params.IndicatorParams;
import com.example.tradingbot.domain.value.TimestampUtc;

import java.time.Instant;
import java.util.Objects;

public final class IndicatorCheckpoint {

    private final Long id;
    private final String indicator;
    private final HistoryGroup historyGroup;
    private final IndicatorParams params;
    private final String version;
    private final TimestampUtc lastTimestamp;

    private IndicatorCheckpoint(
        Long id,
        String indicator,
        HistoryGroup historyGroup,
        IndicatorParams params,
        String version,
        TimestampUtc lastTimestamp
    ) {
        this.id = id;
        this.indicator = indicator;
        this.historyGroup = historyGroup;
        this.params = params;
        this.version = version;
        this.lastTimestamp = lastTimestamp;
    }

    public static IndicatorCheckpoint create(
        String indicator,
        HistoryGroup historyGroup,
        IndicatorParams params,
        String version,
        Instant lastTimestamp
    ) {
        Objects.requireNonNull(indicator, "indicator");
        Objects.requireNonNull(historyGroup, "historyGroup");
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(lastTimestamp, "lastTimestamp");
        return new IndicatorCheckpoint(
            null,
            indicator,
            historyGroup,
            params,
            version,
            TimestampUtc.of(lastTimestamp)
        );
    }

    public IndicatorCheckpoint withId(Long newId) {
        Objects.requireNonNull(newId, "newId");
        return new IndicatorCheckpoint(newId, indicator, historyGroup, params, version, lastTimestamp);
    }

    public static IndicatorCheckpoint rehydrate(
        Long id,
        String indicator,
        HistoryGroup historyGroup,
        IndicatorParams params,
        String version,
        Instant lastTimestamp
    ) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(lastTimestamp, "lastTimestamp");
        return new IndicatorCheckpoint(id, indicator, historyGroup, params, version, TimestampUtc.of(lastTimestamp));
    }

    public Long id() {
        return id;
    }

    public String indicator() {
        return indicator;
    }

    public HistoryGroup historyGroup() {
        return historyGroup;
    }

    public IndicatorParams params() {
        return params;
    }

    public String version() {
        return version;
    }

    public TimestampUtc lastTimestamp() {
        return lastTimestamp;
    }
}
