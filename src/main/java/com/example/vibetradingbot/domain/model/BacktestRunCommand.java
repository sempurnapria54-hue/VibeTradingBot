package com.example.vibetradingbot.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.util.Constants;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Команда запуска бэктеста.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class BacktestRunCommand {

    private UUID exchangeInstrumentId;
    private CanonicalTimeframe timeframe;
    private Instant fromUtc;
    private Instant toUtc;
    private List<String> signalTypes;
    private Long signalParamsId;
    private Long quorumParamsId;
    private Long riskParamsId;
    private Long exchangeParamsId;
    private Long backtestParamsId;
    private JsonNode backtestParams;
    private Boolean allowDuplicate;

    @Builder
    private BacktestRunCommand(UUID exchangeInstrumentId, CanonicalTimeframe timeframe, Instant fromUtc, Instant toUtc,
            List<String> signalTypes, Long signalParamsId, Long quorumParamsId, Long riskParamsId, Long exchangeParamsId,
            Long backtestParamsId, JsonNode backtestParams, Boolean allowDuplicate) {
        this.exchangeInstrumentId = Objects.requireNonNull(exchangeInstrumentId, Constants.Validation.NULL_IDENTIFIER);
        this.timeframe = Objects.requireNonNull(timeframe, Constants.Validation.NULL_TIMEFRAME);
        this.fromUtc = Objects.requireNonNull(fromUtc, Constants.Validation.INVALID_TIME_RANGE);
        this.toUtc = Objects.requireNonNull(toUtc, Constants.Validation.INVALID_TIME_RANGE);
        this.signalTypes = signalTypes == null ? new ArrayList<>() : new ArrayList<>(signalTypes);
        this.signalParamsId = signalParamsId;
        this.quorumParamsId = quorumParamsId;
        this.riskParamsId = riskParamsId;
        this.exchangeParamsId = exchangeParamsId;
        this.backtestParamsId = backtestParamsId;
        this.backtestParams = backtestParams;
        this.allowDuplicate = allowDuplicate;
    }

    public static BacktestRunCommand create(UUID exchangeInstrumentId, CanonicalTimeframe timeframe, Instant fromUtc,
            Instant toUtc, List<String> signalTypes, Long signalParamsId, Long quorumParamsId, Long riskParamsId,
            Long exchangeParamsId, Long backtestParamsId, JsonNode backtestParams, Boolean allowDuplicate) {
        return BacktestRunCommand.builder()
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .fromUtc(fromUtc)
            .toUtc(toUtc)
            .signalTypes(signalTypes)
            .signalParamsId(signalParamsId)
            .quorumParamsId(quorumParamsId)
            .riskParamsId(riskParamsId)
            .exchangeParamsId(exchangeParamsId)
            .backtestParamsId(backtestParamsId)
            .backtestParams(backtestParams)
            .allowDuplicate(allowDuplicate)
            .build();
    }
}
