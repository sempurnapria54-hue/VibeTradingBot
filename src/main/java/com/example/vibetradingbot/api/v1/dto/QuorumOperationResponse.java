package com.example.vibetradingbot.api.v1.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

/**
 * Ответ операции выполнения кворума.
 */
@Getter
@Builder
public class QuorumOperationResponse {

    private final UUID exchangeInstrumentId;
    private final String timeframe;
    private final Long quorumParamsRef;
    private final int insertedDecisions;
    private final int skippedByDebounce;
    private final int skippedByCooldown;
    private final String message;
}
