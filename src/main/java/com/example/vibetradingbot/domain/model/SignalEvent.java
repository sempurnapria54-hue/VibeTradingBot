package com.example.vibetradingbot.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.enums.SignalDirection;
import com.example.vibetradingbot.domain.enums.SignalType;
import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Доменная модель события сигнала.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class SignalEvent {

    private Long id;
    private UUID exchangeInstrumentId;
    private CanonicalTimeframe timeframe;
    private Instant timestamp;
    private SignalType signalType;
    private SignalDirection direction;
    private BigDecimal score;
    private SignalReason reason;
    private long paramsRef;
    private int version;
    private Instant createdAt;

    @Builder
    private SignalEvent(Long id, UUID exchangeInstrumentId, CanonicalTimeframe timeframe, Instant timestamp,
            SignalType signalType, SignalDirection direction, BigDecimal score, SignalReason reason, long paramsRef,
            int version, Instant createdAt) {
        this.id = id;
        this.exchangeInstrumentId = Objects.requireNonNull(exchangeInstrumentId, Constants.Validation.NULL_IDENTIFIER);
        this.timeframe = Objects.requireNonNull(timeframe, Constants.Validation.NULL_TIMEFRAME);
        this.timestamp = Objects.requireNonNull(timestamp, Constants.Validation.INVALID_TIME_RANGE);
        this.signalType = Objects.requireNonNull(signalType, Constants.Validation.NULL_IDENTIFIER);
        this.direction = Objects.requireNonNull(direction, Constants.Validation.NULL_IDENTIFIER);
        this.score = Objects.requireNonNull(score, Constants.Validation.NULL_IDENTIFIER);
        this.reason = reason;
        this.paramsRef = paramsRef;
        this.version = version;
        this.createdAt = createdAt;
    }

    public static SignalEvent create(Long id, UUID exchangeInstrumentId, CanonicalTimeframe timeframe, Instant timestamp,
            SignalType signalType, SignalDirection direction, BigDecimal score, SignalReason reason, long paramsRef,
            int version, Instant createdAt) {
        return SignalEvent.builder()
            .id(id)
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .timestamp(timestamp)
            .signalType(signalType)
            .direction(direction)
            .score(score)
            .reason(reason)
            .paramsRef(paramsRef)
            .version(version)
            .createdAt(createdAt)
            .build();
    }
}
