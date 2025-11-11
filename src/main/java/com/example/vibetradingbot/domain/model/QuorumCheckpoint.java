package com.example.vibetradingbot.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Состояние прогресса кворума по серии.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class QuorumCheckpoint {

    private long quorumParamsId;
    private int version;
    private UUID exchangeInstrumentId;
    private CanonicalTimeframe timeframe;
    private Instant lastTimestamp;
    private Instant updatedAt;

    @Builder
    private QuorumCheckpoint(long quorumParamsId, int version, UUID exchangeInstrumentId,
            CanonicalTimeframe timeframe, Instant lastTimestamp, Instant updatedAt) {
        this.quorumParamsId = quorumParamsId;
        this.version = version;
        this.exchangeInstrumentId = Objects.requireNonNull(exchangeInstrumentId,
                Constants.Validation.NULL_IDENTIFIER);
        this.timeframe = Objects.requireNonNull(timeframe, Constants.Validation.NULL_TIMEFRAME);
        this.lastTimestamp = Objects.requireNonNull(lastTimestamp, Constants.Validation.INVALID_TIME_RANGE);
        this.updatedAt = updatedAt;
    }

    public static QuorumCheckpoint create(long quorumParamsId, int version, UUID exchangeInstrumentId,
            CanonicalTimeframe timeframe, Instant lastTimestamp, Instant updatedAt) {
        return QuorumCheckpoint.builder()
            .quorumParamsId(quorumParamsId)
            .version(version)
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .lastTimestamp(lastTimestamp)
            .updatedAt(updatedAt)
            .build();
    }
}
