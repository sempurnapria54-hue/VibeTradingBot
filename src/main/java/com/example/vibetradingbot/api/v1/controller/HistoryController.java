package com.example.vibetradingbot.api.v1.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vibetradingbot.api.v1.dto.ConsistencyFillRequest;
import com.example.vibetradingbot.api.v1.dto.ConsistencyFillResponse;
import com.example.vibetradingbot.api.v1.dto.ConsistencyGapDto;
import com.example.vibetradingbot.api.v1.dto.ConsistencyScanRequest;
import com.example.vibetradingbot.api.v1.dto.ConsistencyScanResponse;
import com.example.vibetradingbot.api.v1.dto.CoverageDto;
import com.example.vibetradingbot.api.v1.dto.CoverageResponse;
import com.example.vibetradingbot.api.v1.dto.HistoryFillRequest;
import com.example.vibetradingbot.api.v1.dto.HistoryIncrementalRequest;
import com.example.vibetradingbot.api.v1.dto.HistoryOperationResponse;
import com.example.vibetradingbot.api.v1.dto.HistoryRangeRequest;
import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.model.CandleCoverage;
import com.example.vibetradingbot.domain.model.CandleGap;
import com.example.vibetradingbot.domain.model.ConsistencyScanResult;
import com.example.vibetradingbot.domain.model.HistoryOperationResult;
import com.example.vibetradingbot.service.ConsistencyService;
import com.example.vibetradingbot.service.HistoryService;
import com.example.vibetradingbot.util.Constants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Контроллер операций с историей свечей и проверками целостности.
 */
@RestController
@Tag(name = "History")
public class HistoryController {

    private final HistoryService historyService;
    private final ConsistencyService consistencyService;

    public HistoryController(HistoryService historyService, ConsistencyService consistencyService) {
        this.historyService = historyService;
        this.consistencyService = consistencyService;
    }

    @PostMapping(Constants.Api.HISTORY_FILL_PATH)
    @Operation(summary = "Первичная загрузка истории по набору таймфреймов")
    public ResponseEntity<List<HistoryOperationResponse>> fillHistory(@Valid @RequestBody HistoryFillRequest request) {
        List<CanonicalTimeframe> timeframes = CollectionUtils.isEmpty(request.getTimeframes())
            ? null
            : request.getTimeframes().stream().map(CanonicalTimeframe::fromCode).collect(Collectors.toList());
        List<HistoryOperationResult> results = historyService.downloadAll(request.getExchangeInstrumentId(), timeframes);
        List<HistoryOperationResponse> responses = results.stream()
            .map(result -> HistoryOperationResponse.builder()
                .exchangeInstrumentId(result.getExchangeInstrumentId())
                .timeframe(result.getTimeframe().name())
                .insertedBars(result.getInsertedBars())
                .coverageStartUtc(result.getCoverageStart())
                .coverageEndUtc(result.getCoverageEnd())
                .message(Constants.Messages.HISTORY_FILL_COMPLETED)
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping(Constants.Api.HISTORY_FILL_INCREMENTAL_PATH)
    @Operation(summary = "Инкрементальная докачка серии")
    public ResponseEntity<HistoryOperationResponse> incrementalFill(
            @Valid @RequestBody HistoryIncrementalRequest request) {
        HistoryOperationResult result = historyService.downloadIncremental(request.getExchangeInstrumentId(),
                CanonicalTimeframe.fromCode(request.getTimeframe()));
        HistoryOperationResponse response = HistoryOperationResponse.builder()
            .exchangeInstrumentId(result.getExchangeInstrumentId())
            .timeframe(result.getTimeframe().name())
            .insertedBars(result.getInsertedBars())
            .coverageStartUtc(result.getCoverageStart())
            .coverageEndUtc(result.getCoverageEnd())
            .message(Constants.Messages.HISTORY_FILL_COMPLETED)
            .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping(Constants.Api.HISTORY_FILL_RANGE_PATH)
    @Operation(summary = "Целевая докачка диапазона")
    public ResponseEntity<HistoryOperationResponse> fillRange(@Valid @RequestBody HistoryRangeRequest request) {
        HistoryOperationResult result = historyService.refillRange(request.getExchangeInstrumentId(),
                CanonicalTimeframe.fromCode(request.getTimeframe()), request.getFromUtc(), request.getToUtc());
        HistoryOperationResponse response = HistoryOperationResponse.builder()
            .exchangeInstrumentId(result.getExchangeInstrumentId())
            .timeframe(result.getTimeframe().name())
            .insertedBars(result.getInsertedBars())
            .coverageStartUtc(result.getCoverageStart())
            .coverageEndUtc(result.getCoverageEnd())
            .message(Constants.Messages.HISTORY_FILL_COMPLETED)
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping(Constants.Api.COVERAGE_PATH)
    @Operation(summary = "Получить покрытие свечей по таймфреймам")
    public ResponseEntity<CoverageResponse> coverage(@RequestParam UUID exchangeInstrumentId) {
        List<CandleCoverage> coverages = historyService.getCoverage(exchangeInstrumentId);
        List<CoverageDto> dtos = coverages.stream()
            .map(coverage -> CoverageDto.builder()
                .exchangeInstrumentId(coverage.getExchangeInstrumentId())
                .timeframe(coverage.getTimeframe().name())
                .coverageStartUtc(coverage.getCoverageStart())
                .coverageEndUtc(coverage.getCoverageEnd())
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(CoverageResponse.builder().coverages(dtos).build());
    }

    @PostMapping(Constants.Api.CONSISTENCY_SCAN_PATH)
    @Operation(summary = "Сканирование целостности серии")
    public ResponseEntity<ConsistencyScanResponse> scanConsistency(@Valid @RequestBody ConsistencyScanRequest request) {
        ConsistencyScanResult result = consistencyService.scan(request.getExchangeInstrumentId(),
                CanonicalTimeframe.fromCode(request.getTimeframe()), request.getWindowBars());
        ConsistencyScanResponse response = ConsistencyScanResponse.builder()
            .exchangeInstrumentId(result.getExchangeInstrumentId())
            .timeframe(result.getTimeframe().name())
            .gaps(result.getGaps().stream()
                .map(gap -> ConsistencyGapDto.builder()
                    .fromUtc(gap.getFromUtc())
                    .toUtc(gap.getToUtc())
                    .build())
                .collect(Collectors.toList()))
            .missingBars(result.getMissingBars())
            .misalignedCount(result.getMisalignedCount())
            .lastCandleUtc(result.getLastCandleUtc())
            .message(Constants.Messages.CONSISTENCY_SCAN_COMPLETED)
            .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping(Constants.Api.CONSISTENCY_FILL_MISSING_PATH)
    @Operation(summary = "Докачка пропусков истории")
    public ResponseEntity<ConsistencyFillResponse> fillMissing(@Valid @RequestBody ConsistencyFillRequest request) {
        List<CandleGap> gaps = CollectionUtils.isEmpty(request.getGaps()) ? List.of()
            : request.getGaps().stream()
                .map(gap -> CandleGap.create(gap.getFromUtc(), gap.getToUtc()))
                .collect(Collectors.toList());
        List<HistoryOperationResult> results = consistencyService.gapFill(request.getExchangeInstrumentId(),
                CanonicalTimeframe.fromCode(request.getTimeframe()), gaps);
        ConsistencyFillResponse response = ConsistencyFillResponse.builder()
            .exchangeInstrumentId(request.getExchangeInstrumentId())
            .timeframe(request.getTimeframe())
            .processedGaps(results.size())
            .message(Constants.Messages.CONSISTENCY_FILL_COMPLETED)
            .build();
        return ResponseEntity.ok(response);
    }
}
