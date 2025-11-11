package com.example.vibetradingbot.domain.model;

import java.util.UUID;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.enums.SignalType;

import lombok.Builder;
import lombok.Getter;

/**
 * Результат генерации сигналов по серии.
 */
@Getter
@Builder
public class SignalOperationResult {

    private final UUID exchangeInstrumentId;
    private final CanonicalTimeframe timeframe;
    private final SignalType signalType;
    private final long paramsRef;
    private final int insertedSignals;
    private final int skippedByDebounce;
    private final int skippedByCooldown;
}
