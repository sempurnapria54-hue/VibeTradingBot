package com.example.vibetradingbot.api.v1.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

/**
 * Представление решения кворума для API.
 */
@Getter
@Builder
public class QuorumDecisionDto {

    private final Long id;
    private final UUID exchangeInstrumentId;
    private final String timeframe;
    private final Instant timestamp;
    private final String action;
    private final String direction;
    private final BigDecimal score;
    private final BigDecimal thresholdEnter;
    private final BigDecimal thresholdExit;
    private final Long quorumParamsId;
    private final Integer version;
    private final Map<String, Object> positionState;
    private final Map<String, Object> reason;
    private final Instant createdAt;
}
