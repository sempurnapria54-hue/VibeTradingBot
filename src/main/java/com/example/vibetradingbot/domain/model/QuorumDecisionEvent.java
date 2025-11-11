package com.example.vibetradingbot.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.enums.Direction;
import com.example.vibetradingbot.domain.enums.QuorumAction;
import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Доменная модель решения кворума.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class QuorumDecisionEvent {

    private Long id;
    private UUID exchangeInstrumentId;
    private CanonicalTimeframe timeframe;
    private Instant timestamp;
    private QuorumAction action;
    private Direction direction;
    private BigDecimal score;
    private BigDecimal thresholdEnter;
    private BigDecimal thresholdExit;
    private long quorumParamsId;
    private int version;
    private Map<String, Object> positionState;
    private QuorumReason reason;
    private Instant createdAt;

    @Builder
    private QuorumDecisionEvent(Long id, UUID exchangeInstrumentId, CanonicalTimeframe timeframe, Instant timestamp,
            QuorumAction action, Direction direction, BigDecimal score, BigDecimal thresholdEnter,
            BigDecimal thresholdExit, long quorumParamsId, int version, Map<String, Object> positionState,
            QuorumReason reason, Instant createdAt) {
        this.id = id;
        this.exchangeInstrumentId = Objects.requireNonNull(exchangeInstrumentId,
                Constants.Validation.NULL_IDENTIFIER);
        this.timeframe = Objects.requireNonNull(timeframe, Constants.Validation.NULL_TIMEFRAME);
        this.timestamp = Objects.requireNonNull(timestamp, Constants.Validation.INVALID_TIME_RANGE);
        this.action = Objects.requireNonNull(action, Constants.Validation.NULL_IDENTIFIER);
        this.direction = direction;
        this.score = Objects.requireNonNull(score, Constants.Validation.NULL_IDENTIFIER);
        this.thresholdEnter = Objects.requireNonNull(thresholdEnter, Constants.Validation.NULL_IDENTIFIER);
        this.thresholdExit = Objects.requireNonNull(thresholdExit, Constants.Validation.NULL_IDENTIFIER);
        this.quorumParamsId = quorumParamsId;
        this.version = version;
        this.positionState = positionState;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static QuorumDecisionEvent create(Long id, UUID exchangeInstrumentId, CanonicalTimeframe timeframe,
            Instant timestamp, QuorumAction action, Direction direction, BigDecimal score,
            BigDecimal thresholdEnter, BigDecimal thresholdExit, long quorumParamsId, int version,
            Map<String, Object> positionState, QuorumReason reason, Instant createdAt) {
        return QuorumDecisionEvent.builder()
            .id(id)
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .timestamp(timestamp)
            .action(action)
            .direction(direction)
            .score(score)
            .thresholdEnter(thresholdEnter)
            .thresholdExit(thresholdExit)
            .quorumParamsId(quorumParamsId)
            .version(version)
            .positionState(positionState)
            .reason(reason)
            .createdAt(createdAt)
            .build();
    }
}
