package com.example.vibetradingbot.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import com.example.vibetradingbot.config.BacktestProperties;
import com.example.vibetradingbot.domain.model.BacktestEquityPoint;
import com.example.vibetradingbot.domain.model.BacktestPerformanceAggregate;
import com.example.vibetradingbot.domain.model.BacktestReport;
import com.example.vibetradingbot.domain.model.BacktestRun;
import com.example.vibetradingbot.domain.model.BacktestRunCommand;
import com.example.vibetradingbot.domain.model.BacktestTrade;
import com.example.vibetradingbot.domain.model.BacktestTradePage;
import com.example.vibetradingbot.persistence.repository.BacktestRepository;
import com.example.vibetradingbot.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

/**
 * Сервис оффлайн-бэктеста. Пока реализует каркас с фиксацией конфигураций и пустыми результатами.
 */
@Service
public class BacktestService {

    private static final BigDecimal ZERO_DECIMAL = BigDecimal.ZERO;

    private final BacktestRepository backtestRepository;
    private final BacktestProperties backtestProperties;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public BacktestService(BacktestRepository backtestRepository, BacktestProperties backtestProperties,
            ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.backtestRepository = backtestRepository;
        this.backtestProperties = backtestProperties;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    public long run(BacktestRunCommand command) {
        Optional<Long> existingRun = Optional.empty();
        if (BooleanUtils.isNotTrue(command.getAllowDuplicate())) {
            existingRun = backtestRepository.findExistingRun(command.getTimeframe(), command.getExchangeInstrumentId(),
                    command.getFromUtc(), command.getToUtc(), command.getBacktestParamsId());
        }
        if (existingRun.isPresent()) {
            return existingRun.get();
        }
        String paramsSnapshot = serializeParams(command.getBacktestParams());
        BacktestRun run = BacktestRun.create(command.getExchangeInstrumentId(), command.getTimeframe(),
                command.getFromUtc(), command.getToUtc(), command.getSignalTypes(), command.getSignalParamsId(),
                command.getQuorumParamsId(), command.getRiskParamsId(), command.getExchangeParamsId(),
                command.getBacktestParamsId(), paramsSnapshot, Instant.now());
        long runId = backtestRepository.insertRun(run);
        persistEmptyResults(runId, run);
        return runId;
    }

    public BacktestReport getReport(long runId) {
        Optional<BacktestRun> run = backtestRepository.findRunById(runId);
        BacktestPerformanceAggregate aggregate = backtestRepository.findPerformance(runId)
            .orElseGet(() -> BacktestPerformanceAggregate.create(runId, 0, ZERO_DECIMAL, ZERO_DECIMAL, ZERO_DECIMAL,
                    ZERO_DECIMAL, null, null));
        return BacktestReport.create(run.orElseThrow(() -> new IllegalArgumentException("Run not found")), aggregate);
    }

    public List<BacktestEquityPoint> getEquity(long runId) {
        return backtestRepository.findEquity(runId);
    }

    public BacktestTradePage getTrades(long runId, int page, int size) {
        return backtestRepository.findTrades(runId, page, size);
    }

    private void persistEmptyResults(long runId, BacktestRun run) {
        BacktestPerformanceAggregate aggregate = BacktestPerformanceAggregate.create(runId, 0, ZERO_DECIMAL, ZERO_DECIMAL,
                ZERO_DECIMAL, ZERO_DECIMAL, null, null);
        backtestRepository.insertPerformance(aggregate);
        BacktestEquityPoint startPoint = BacktestEquityPoint.create(runId, run.getFromUtc(), ZERO_DECIMAL);
        backtestRepository.insertEquityPoints(Collections.singletonList(startPoint));
        backtestRepository.insertTrades(Collections.emptyList());
        recordMetrics(runId, aggregate, run);
    }

    private void recordMetrics(long runId, BacktestPerformanceAggregate aggregate, BacktestRun run) {
        Tags tags = Tags.of("runId", String.valueOf(runId));
        meterRegistry.counter(Constants.Metrics.BACKTEST_TRADES_TOTAL, tags)
            .increment(Optional.ofNullable(aggregate.getTrades()).orElse(0));
        meterRegistry.counter(Constants.Metrics.BACKTEST_TRADES_TP, tags).increment(0);
        meterRegistry.counter(Constants.Metrics.BACKTEST_TRADES_SL, tags).increment(0);
        meterRegistry.counter(Constants.Metrics.BACKTEST_TRADES_TIME, tags).increment(0);
        double maxDd = Optional.ofNullable(aggregate.getMaxDrawdownR()).map(BigDecimal::doubleValue).orElse(0d);
        meterRegistry.summary(Constants.Metrics.BACKTEST_EQUITY_MAX_DD, tags).record(maxDd);
        meterRegistry.summary(Constants.Metrics.BACKTEST_EQUITY_FINAL, tags).record(0d);
        double durationMs = Duration.between(run.getFromUtc(), run.getToUtc()).toMillis();
        meterRegistry.summary(Constants.Metrics.BACKTEST_RUN_DURATION, tags).record(durationMs);
        meterRegistry.summary(Constants.Metrics.BACKTEST_RUN_BARS_PROCESSED, tags)
            .record(calculateBarsProcessed(run.getFromUtc(), run.getToUtc()));
    }

    private double calculateBarsProcessed(Instant fromUtc, Instant toUtc) {
        long seconds = Duration.between(fromUtc, toUtc).getSeconds();
        if (seconds <= 0) {
            return 0;
        }
        return (double) seconds;
    }

    private String serializeParams(JsonNode node) {
        if (node == null || node.isNull()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            return node.toString();
        }
    }

    public BacktestProperties getBacktestProperties() {
        return backtestProperties;
    }
}
