package com.example.vibetradingbot.domain.model;

import java.util.UUID;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;

import lombok.Builder;
import lombok.Getter;

/**
 * Результат выполнения кворумного решения по серии.
 */
@Getter
@Builder
public class QuorumOperationResult {

    private final UUID exchangeInstrumentId;
    private final CanonicalTimeframe timeframe;
    private final long quorumParamsRef;
    private final int insertedDecisions;
    private final int skippedByDebounce;
    private final int skippedByCooldown;
}
