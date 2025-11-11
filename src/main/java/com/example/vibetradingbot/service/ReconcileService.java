package com.example.vibetradingbot.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.example.vibetradingbot.config.ReconcileProperties;
import com.example.vibetradingbot.domain.model.OrderSnapshot;
import com.example.vibetradingbot.domain.model.PositionSnapshot;
import com.example.vibetradingbot.exchange.connector.ExecutionAdapter;
import com.example.vibetradingbot.util.Constants;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Сервис сверки локального состояния с биржей.
 */
@Service
public class ReconcileService {

    private final ExecutionAdapter executionAdapter;
    private final ReconcileProperties reconcileProperties;
    private final MeterRegistry meterRegistry;

    public ReconcileService(ExecutionAdapter executionAdapter,
        ReconcileProperties reconcileProperties,
        MeterRegistry meterRegistry) {
        this.executionAdapter = Objects.requireNonNull(executionAdapter, Constants.Validation.NULL_IDENTIFIER);
        this.reconcileProperties = Objects.requireNonNull(reconcileProperties, Constants.Validation.NULL_IDENTIFIER);
        this.meterRegistry = Objects.requireNonNull(meterRegistry, Constants.Validation.NULL_IDENTIFIER);
    }

    public void reconcileNow() {
        if (!reconcileProperties.isEnabled()) {
            return;
        }
        List<OrderSnapshot> openOrders = executionAdapter.listOpenOrders(null);
        List<PositionSnapshot> positions = executionAdapter.getPositions();
        if (openOrders.isEmpty() && positions.isEmpty()) {
            meterRegistry.counter(Constants.Metrics.LIVE_RECONCILE_FIXED).increment();
            return;
        }
        meterRegistry.counter(Constants.Metrics.LIVE_RECONCILE_DISCREPANCIES).increment();
    }

    public boolean isEnabled() {
        return reconcileProperties.isEnabled();
    }
}
