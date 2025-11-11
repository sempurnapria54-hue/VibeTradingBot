package com.example.vibetradingbot.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.vibetradingbot.config.QuorumProperties;
import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.enums.Direction;
import com.example.vibetradingbot.domain.enums.QuorumAction;
import com.example.vibetradingbot.domain.model.QuorumCheckpoint;
import com.example.vibetradingbot.domain.model.QuorumDecisionEvent;
import com.example.vibetradingbot.domain.model.QuorumOperationResult;
import com.example.vibetradingbot.domain.model.QuorumParamsActivationResult;
import com.example.vibetradingbot.domain.model.QuorumReason;
import com.example.vibetradingbot.util.Constants;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

/**
 * Сервис кворума. Пока реализует каркас с метриками и пустыми ответами.
 */
@Service
public class QuorumService {

    private final QuorumProperties properties;
    private final MeterRegistry meterRegistry;

    public QuorumService(QuorumProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    public QuorumOperationResult decide(UUID exchangeInstrumentId, CanonicalTimeframe timeframe, long quorumParamsRef) {
        QuorumOperationResult result = QuorumOperationResult.builder()
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .quorumParamsRef(quorumParamsRef)
            .insertedDecisions(0)
            .skippedByDebounce(0)
            .skippedByCooldown(0)
            .build();
        incrementMetrics(QuorumAction.HOLD, result);
        return result;
    }

    public QuorumOperationResult decideRange(UUID exchangeInstrumentId, CanonicalTimeframe timeframe, long quorumParamsRef,
            Instant fromUtc, Instant toUtc) {
        QuorumOperationResult result = QuorumOperationResult.builder()
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .quorumParamsRef(quorumParamsRef)
            .insertedDecisions(0)
            .skippedByDebounce(0)
            .skippedByCooldown(0)
            .build();
        incrementMetrics(QuorumAction.HOLD, result);
        return result;
    }

    public List<QuorumDecisionEvent> findLatestDecisions(UUID exchangeInstrumentId, CanonicalTimeframe timeframe,
            int limit) {
        return Collections.emptyList();
    }

    public Optional<QuorumCheckpoint> findCheckpoint(UUID exchangeInstrumentId, CanonicalTimeframe timeframe,
            long quorumParamsRef) {
        return Optional.empty();
    }

    public QuorumParamsActivationResult activateParams(String version, String canonicalJson) {
        long generatedId = Math.abs((long) canonicalJson.hashCode()) + Instant.now().toEpochMilli();
        return QuorumParamsActivationResult.builder()
            .quorumParamsId(generatedId)
            .version(version)
            .build();
    }

    private void incrementMetrics(QuorumAction action, QuorumOperationResult result) {
        Tags actionTags = Tags.of("action", action.name());
        meterRegistry.counter(Constants.Metrics.QUORUM_DECISIONS_TOTAL, actionTags)
            .increment(result.getInsertedDecisions());
        meterRegistry.summary(Constants.Metrics.QUORUM_SCORE_RAW)
            .record(0);
        meterRegistry.summary(Constants.Metrics.QUORUM_SCORE_FILTERED)
            .record(0);
        meterRegistry.counter(Constants.Metrics.QUORUM_CHECKPOINT_UPDATED)
            .increment(0);
    }

    public QuorumProperties getProperties() {
        return properties;
    }

    public QuorumReason buildEmptyReason() {
        return QuorumReason.builder()
            .action(QuorumAction.HOLD)
            .direction(Direction.NEUTRAL)
            .notes("no decisions")
            .build();
    }
}
