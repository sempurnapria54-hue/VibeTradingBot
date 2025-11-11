package com.example.vibetradingbot.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.vibetradingbot.config.SignalProperties;
import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.enums.SignalType;
import com.example.vibetradingbot.domain.model.SignalCheckpoint;
import com.example.vibetradingbot.domain.model.SignalEvent;
import com.example.vibetradingbot.domain.model.SignalOperationResult;
import com.example.vibetradingbot.util.Constants;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

/**
 * Сервис генерации сигналов. Пока содержит каркас и метрики, без реальной бизнес-логики.
 */
@Service
public class SignalService {

    private final SignalProperties properties;
    private final MeterRegistry meterRegistry;

    public SignalService(SignalProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    public SignalOperationResult generate(UUID exchangeInstrumentId, CanonicalTimeframe timeframe, SignalType signalType,
            long paramsRef) {
        SignalOperationResult result = SignalOperationResult.builder()
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .signalType(signalType)
            .paramsRef(paramsRef)
            .insertedSignals(0)
            .skippedByDebounce(0)
            .skippedByCooldown(0)
            .build();
        incrementMetrics(signalType, result);
        return result;
    }

    public SignalOperationResult generateRange(UUID exchangeInstrumentId, CanonicalTimeframe timeframe,
            SignalType signalType, long paramsRef, Instant fromUtc, Instant toUtc) {
        SignalOperationResult result = SignalOperationResult.builder()
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .signalType(signalType)
            .paramsRef(paramsRef)
            .insertedSignals(0)
            .skippedByDebounce(0)
            .skippedByCooldown(0)
            .build();
        incrementMetrics(signalType, result);
        return result;
    }

    public List<SignalEvent> findLatestSignals(UUID exchangeInstrumentId, CanonicalTimeframe timeframe,
            SignalType signalType, int limit) {
        return Collections.emptyList();
    }

    public Optional<SignalCheckpoint> findCheckpoint(SignalType signalType, CanonicalTimeframe timeframe,
            UUID exchangeInstrumentId, long paramsRef) {
        return Optional.empty();
    }

    private void incrementMetrics(SignalType signalType, SignalOperationResult result) {
        Tags tags = Tags.of("signalType", signalType.name());
        meterRegistry.counter(Constants.Metrics.SIGNALS_GENERATED_TOTAL, tags).increment(result.getInsertedSignals());
        meterRegistry.counter(Constants.Metrics.SIGNALS_DEBOUNCE_SKIP, tags).increment(result.getSkippedByDebounce());
        meterRegistry.counter(Constants.Metrics.SIGNALS_COOLDOWN_SKIP, tags).increment(result.getSkippedByCooldown());
    }

    public SignalProperties getProperties() {
        return properties;
    }
}
