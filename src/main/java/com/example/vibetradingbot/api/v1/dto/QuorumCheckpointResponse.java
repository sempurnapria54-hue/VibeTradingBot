package com.example.vibetradingbot.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

/**
 * Ответ с чекпоинтом кворума.
 */
@Getter
@Builder
public class QuorumCheckpointResponse {

    private final Long quorumParamsId;
    private final Integer version;
    private final UUID exchangeInstrumentId;
    private final String timeframe;
    private final Instant lastTimestamp;
    private final Instant updatedAt;
}
