package com.example.vibetradingbot.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import com.example.vibetradingbot.config.RiskProperties;
import com.example.vibetradingbot.domain.enums.RiskHaltScope;
import com.example.vibetradingbot.domain.model.NewOrder;
import com.example.vibetradingbot.domain.model.RiskCheckResult;
import com.example.vibetradingbot.domain.model.RiskHaltState;
import com.example.vibetradingbot.util.Constants;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Сервис управления рисками и флагами остановки торговли.
 */
@Service
public class RiskService {

    private final RiskProperties riskProperties;
    private final MeterRegistry meterRegistry;
    private final Map<String, RiskHaltState> halts;
    private final AtomicLong activeHalts;

    public RiskService(RiskProperties riskProperties, MeterRegistry meterRegistry) {
        this.riskProperties = Objects.requireNonNull(riskProperties, Constants.Validation.NULL_IDENTIFIER);
        this.meterRegistry = Objects.requireNonNull(meterRegistry, Constants.Validation.NULL_IDENTIFIER);
        this.halts = new ConcurrentHashMap<>();
        this.activeHalts = new AtomicLong(0L);
        meterRegistry.gauge(Constants.Metrics.LIVE_RISK_HALT_ACTIVE, activeHalts);
    }

    public RiskCheckResult preCheck(NewOrder newOrder) {
        Objects.requireNonNull(newOrder, Constants.Validation.NULL_IDENTIFIER);
        if (isHalted(RiskHaltScope.GLOBAL, null)) {
            meterRegistry.counter(Constants.Metrics.LIVE_RISK_VIOLATIONS).increment();
            return RiskCheckResult.deny(Constants.Errors.LIVE_HALTED);
        }
        if (isHalted(RiskHaltScope.INSTRUMENT, newOrder.getExchangeInstrumentId())) {
            meterRegistry.counter(Constants.Metrics.LIVE_RISK_VIOLATIONS).increment();
            return RiskCheckResult.deny(Constants.Errors.LIVE_HALTED);
        }
        if (BooleanUtils.isFalse(riskProperties.isIsolated())) {
            meterRegistry.counter(Constants.Metrics.LIVE_RISK_VIOLATIONS).increment();
            return RiskCheckResult.deny(Constants.Errors.RISK_VIOLATION);
        }
        if (riskProperties.getRiskPerTradePct() > 1.0d) {
            meterRegistry.counter(Constants.Metrics.LIVE_RISK_VIOLATIONS).increment();
            return RiskCheckResult.deny(Constants.Errors.RISK_VIOLATION);
        }
        if (riskProperties.getLeverageMax() > 10) {
            meterRegistry.counter(Constants.Metrics.LIVE_RISK_VIOLATIONS).increment();
            return RiskCheckResult.deny(Constants.Errors.RISK_VIOLATION);
        }
        return RiskCheckResult.allow();
    }

    public void haltTrading(RiskHaltScope scope, Long exchangeInstrumentId, String reason) {
        Objects.requireNonNull(scope, Constants.Validation.NULL_IDENTIFIER);
        String key = buildKey(scope, exchangeInstrumentId);
        RiskHaltState state = RiskHaltState.create(scope, exchangeInstrumentId, true, reason, Instant.now());
        halts.put(key, state);
        activeHalts.set(halts.values().stream().filter(RiskHaltState::isHalted).count());
    }

    public void resumeTrading(RiskHaltScope scope, Long exchangeInstrumentId) {
        Objects.requireNonNull(scope, Constants.Validation.NULL_IDENTIFIER);
        String key = buildKey(scope, exchangeInstrumentId);
        RiskHaltState state = RiskHaltState.create(scope, exchangeInstrumentId, false, Constants.Messages.LIVE_HALT_DISABLED, Instant.now());
        halts.put(key, state);
        activeHalts.set(halts.values().stream().filter(RiskHaltState::isHalted).count());
    }

    public List<RiskHaltState> getHalts() {
        return new ArrayList<>(halts.values());
    }

    public boolean isHalted(RiskHaltScope scope, Long exchangeInstrumentId) {
        Objects.requireNonNull(scope, Constants.Validation.NULL_IDENTIFIER);
        String key = buildKey(scope, exchangeInstrumentId);
        RiskHaltState state = halts.get(key);
        if (Objects.isNull(state)) {
            if (scope == RiskHaltScope.GLOBAL) {
                return false;
            }
            RiskHaltState globalState = halts.get(buildKey(RiskHaltScope.GLOBAL, null));
            if (Objects.nonNull(globalState)) {
                return globalState.isHalted();
            }
            return false;
        }
        if (state.isHalted()) {
            return true;
        }
        RiskHaltState globalState = halts.get(buildKey(RiskHaltScope.GLOBAL, null));
        return Objects.nonNull(globalState) && globalState.isHalted();
    }

    private String buildKey(RiskHaltScope scope, Long exchangeInstrumentId) {
        if (Objects.isNull(exchangeInstrumentId)) {
            return scope.name();
        }
        return scope.name() + ":" + exchangeInstrumentId;
    }
}
