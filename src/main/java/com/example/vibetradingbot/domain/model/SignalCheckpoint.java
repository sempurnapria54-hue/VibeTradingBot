package com.example.vibetradingbot.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.enums.SignalType;
import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Чекпоинт генерации сигналов по серии.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class SignalCheckpoint {

    private SignalType signalType;
    private CanonicalTimeframe timeframe;
    private UUID exchangeInstrumentId;
    private long paramsRef;
    private int version;
    private Instant lastTimestamp;
    private Instant updatedAt;

    @Builder
    private SignalCheckpoint(SignalType signalType, CanonicalTimeframe timeframe, UUID exchangeInstrumentId,
            long paramsRef, int version, Instant lastTimestamp, Instant updatedAt) {
        this.signalType = Objects.requireNonNull(signalType, Constants.Validation.NULL_IDENTIFIER);
        this.timeframe = Objects.requireNonNull(timeframe, Constants.Validation.NULL_TIMEFRAME);
        this.exchangeInstrumentId = Objects.requireNonNull(exchangeInstrumentId, Constants.Validation.NULL_IDENTIFIER);
        this.paramsRef = paramsRef;
        this.version = version;
        this.lastTimestamp = lastTimestamp;
        this.updatedAt = updatedAt;
    }

    public static SignalCheckpoint create(SignalType signalType, CanonicalTimeframe timeframe, UUID exchangeInstrumentId,
            long paramsRef, int version, Instant lastTimestamp, Instant updatedAt) {
        return SignalCheckpoint.builder()
            .signalType(signalType)
            .timeframe(timeframe)
            .exchangeInstrumentId(exchangeInstrumentId)
            .paramsRef(paramsRef)
            .version(version)
            .lastTimestamp(lastTimestamp)
            .updatedAt(updatedAt)
            .build();
    }
}
