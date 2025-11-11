package com.example.vibetradingbot.api.v1.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vibetradingbot.api.v1.dto.BacktestEquityPointDto;
import com.example.vibetradingbot.api.v1.dto.BacktestEquityResponse;
import com.example.vibetradingbot.api.v1.dto.BacktestPerformanceDto;
import com.example.vibetradingbot.api.v1.dto.BacktestReportResponse;
import com.example.vibetradingbot.api.v1.dto.BacktestRunRequest;
import com.example.vibetradingbot.api.v1.dto.BacktestRunResponse;
import com.example.vibetradingbot.api.v1.dto.BacktestTradeDto;
import com.example.vibetradingbot.api.v1.dto.BacktestTradesResponse;
import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.model.BacktestEquityPoint;
import com.example.vibetradingbot.domain.model.BacktestReport;
import com.example.vibetradingbot.domain.model.BacktestRunCommand;
import com.example.vibetradingbot.domain.model.BacktestTrade;
import com.example.vibetradingbot.domain.model.BacktestTradePage;
import com.example.vibetradingbot.service.BacktestService;
import com.example.vibetradingbot.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Контроллер API бэктеста.
 */
@RestController
@Tag(name = "Backtest")
public class BacktestController {

    private static final int DEFAULT_PAGE_SIZE = 50;

    private final BacktestService backtestService;
    private final ObjectMapper objectMapper;

    public BacktestController(BacktestService backtestService, ObjectMapper objectMapper) {
        this.backtestService = backtestService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(Constants.Api.BACKTEST_RUN_PATH)
    @Operation(summary = "Запустить оффлайн-бэктест")
    public ResponseEntity<BacktestRunResponse> run(@Valid @RequestBody BacktestRunRequest request) {
        BacktestRunCommand command = BacktestRunCommand.create(request.getExchangeInstrumentId(),
                CanonicalTimeframe.fromCode(request.getTimeframe()), request.getFromUtc(), request.getToUtc(),
                request.getSignalTypes(), request.getSignalParamsId(), request.getQuorumParamsId(),
                request.getRiskParamsId(), request.getExchangeParamsId(), request.getBacktestParamsId(),
                request.getBacktestParams(), request.getAllowDuplicate());
        long runId = backtestService.run(command);
        return ResponseEntity.ok(BacktestRunResponse.builder()
            .runId(runId)
            .build());
    }

    @GetMapping(Constants.Api.BACKTEST_REPORT_PATH)
    @Operation(summary = "Получить отчёт бэктеста")
    public ResponseEntity<BacktestReportResponse> report(@PathVariable("runId") Long runId) {
        BacktestReport report = backtestService.getReport(runId);
        BacktestReportResponse response = BacktestReportResponse.builder()
            .runId(report.getRun().getId())
            .exchangeInstrumentId(report.getRun().getExchangeInstrumentId())
            .timeframe(report.getRun().getTimeframe().name())
            .fromUtc(report.getRun().getFromUtc())
            .toUtc(report.getRun().getToUtc())
            .signalTypes(report.getRun().getSignalTypes())
            .signalParamsId(report.getRun().getSignalParamsId())
            .quorumParamsId(report.getRun().getQuorumParamsId())
            .riskParamsId(report.getRun().getRiskParamsId())
            .exchangeParamsId(report.getRun().getExchangeParamsId())
            .backtestParamsId(report.getRun().getBacktestParamsId())
            .paramsSnapshot(parseParams(report.getRun().getParamsSnapshot()))
            .performance(toDto(report.getAggregate()))
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping(Constants.Api.BACKTEST_EQUITY_PATH)
    @Operation(summary = "Получить кривую капитала бэктеста")
    public ResponseEntity<BacktestEquityResponse> equity(@PathVariable("runId") Long runId) {
        List<BacktestEquityPoint> points = backtestService.getEquity(runId);
        List<BacktestEquityPointDto> dtoList = new ArrayList<>();
        for (BacktestEquityPoint point : points) {
            dtoList.add(BacktestEquityPointDto.builder()
                .timestamp(point.getTimestamp())
                .equityR(point.getEquityR())
                .build());
        }
        return ResponseEntity.ok(BacktestEquityResponse.builder()
            .points(dtoList)
            .build());
    }

    @GetMapping(Constants.Api.BACKTEST_TRADES_PATH)
    @Operation(summary = "Получить сделки бэктеста")
    public ResponseEntity<BacktestTradesResponse> trades(@PathVariable("runId") Long runId,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        int resolvedPage = Objects.nonNull(page) && page >= 0 ? page : 0;
        int resolvedSize = Objects.nonNull(size) && size > 0 ? size : DEFAULT_PAGE_SIZE;
        BacktestTradePage tradePage = backtestService.getTrades(runId, resolvedPage, resolvedSize);
        List<BacktestTradeDto> trades = new ArrayList<>();
        for (BacktestTrade trade : tradePage.getTrades()) {
            trades.add(mapTrade(trade));
        }
        BacktestTradesResponse response = BacktestTradesResponse.builder()
            .trades(trades)
            .total(tradePage.getTotal())
            .page(tradePage.getPage())
            .size(tradePage.getSize())
            .build();
        return ResponseEntity.ok(response);
    }

    private BacktestPerformanceDto toDto(com.example.vibetradingbot.domain.model.BacktestPerformanceAggregate aggregate) {
        if (aggregate == null) {
            return BacktestPerformanceDto.builder().build();
        }
        return BacktestPerformanceDto.builder()
            .trades(aggregate.getTrades())
            .winrate(aggregate.getWinrate())
            .profitFactor(aggregate.getProfitFactor())
            .expectancyR(aggregate.getExpectancyR())
            .maxDrawdownR(aggregate.getMaxDrawdownR())
            .sharpe(aggregate.getSharpe())
            .sortino(aggregate.getSortino())
            .build();
    }

    private Map<String, Object> parseParams(String paramsSnapshot) {
        if (paramsSnapshot == null || paramsSnapshot.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(paramsSnapshot, Map.class);
        } catch (IOException ex) {
            return Map.of("raw", paramsSnapshot);
        }
    }

    private BacktestTradeDto mapTrade(BacktestTrade trade) {
        Object reason = parseReason(trade.getReason());
        return BacktestTradeDto.builder()
            .id(trade.getId())
            .signalId(trade.getSignalId())
            .entryTs(trade.getEntryTimestamp())
            .exitTs(trade.getExitTimestamp())
            .side(trade.getSide() == null ? null : trade.getSide().name())
            .entryPrice(trade.getEntryPrice())
            .exitPrice(trade.getExitPrice())
            .pnlR(trade.getPnlR())
            .fees(trade.getFees())
            .slippage(trade.getSlippage())
            .funding(trade.getFunding())
            .maeR(trade.getMaeR())
            .mfeR(trade.getMfeR())
            .exitReason(trade.getExitReason() == null ? null : trade.getExitReason().name())
            .timeToEventBars(trade.getTimeToEventBars())
            .reason(reason)
            .build();
    }

    private Object parseReason(String reason) {
        if (reason == null || reason.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(reason, Object.class);
        } catch (IOException ex) {
            return Map.of("raw", reason);
        }
    }
}
