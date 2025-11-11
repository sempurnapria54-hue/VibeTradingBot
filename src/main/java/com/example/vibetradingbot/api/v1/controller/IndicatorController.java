package com.example.vibetradingbot.api.v1.controller;

import java.util.Optional;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vibetradingbot.api.v1.dto.IndicatorCheckpointResponse;
import com.example.vibetradingbot.api.v1.dto.IndicatorFillRequest;
import com.example.vibetradingbot.api.v1.dto.IndicatorFillResponse;
import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.enums.IndicatorType;
import com.example.vibetradingbot.domain.model.IndicatorCheckpoint;
import com.example.vibetradingbot.domain.model.IndicatorOperationResult;
import com.example.vibetradingbot.service.IndicatorService;
import com.example.vibetradingbot.util.Constants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Контроллер для запуска расчёта индикаторов и получения чекпоинтов.
 */
@RestController
@Tag(name = "Indicators")
public class IndicatorController {

    private final IndicatorService indicatorService;

    public IndicatorController(IndicatorService indicatorService) {
        this.indicatorService = indicatorService;
    }

    @PostMapping(Constants.Api.INDICATOR_FILL_EMA_PATH)
    @Operation(summary = "Расчёт значений EMA")
    public ResponseEntity<IndicatorFillResponse> fillEma(@Valid @RequestBody IndicatorFillRequest request) {
        IndicatorOperationResult result = indicatorService.fillEma(request.getExchangeInstrumentId(),
                CanonicalTimeframe.fromCode(request.getTimeframe()), request.getParamsRef());
        IndicatorFillResponse response = buildFillResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping(Constants.Api.INDICATOR_FILL_MACD_PATH)
    @Operation(summary = "Расчёт значений MACD")
    public ResponseEntity<IndicatorFillResponse> fillMacd(@Valid @RequestBody IndicatorFillRequest request) {
        IndicatorOperationResult result = indicatorService.fillMacd(request.getExchangeInstrumentId(),
                CanonicalTimeframe.fromCode(request.getTimeframe()), request.getParamsRef());
        IndicatorFillResponse response = buildFillResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping(Constants.Api.INDICATOR_FILL_RSI_PATH)
    @Operation(summary = "Расчёт значений RSI")
    public ResponseEntity<IndicatorFillResponse> fillRsi(@Valid @RequestBody IndicatorFillRequest request) {
        IndicatorOperationResult result = indicatorService.fillRsi(request.getExchangeInstrumentId(),
                CanonicalTimeframe.fromCode(request.getTimeframe()), request.getParamsRef());
        IndicatorFillResponse response = buildFillResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping(Constants.Api.INDICATOR_FILL_STOCH_PATH)
    @Operation(summary = "Расчёт значений Stochastic")
    public ResponseEntity<IndicatorFillResponse> fillStoch(@Valid @RequestBody IndicatorFillRequest request) {
        IndicatorOperationResult result = indicatorService.fillStoch(request.getExchangeInstrumentId(),
                CanonicalTimeframe.fromCode(request.getTimeframe()), request.getParamsRef());
        IndicatorFillResponse response = buildFillResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping(Constants.Api.INDICATOR_FILL_BB_PATH)
    @Operation(summary = "Расчёт значений Bollinger Bands")
    public ResponseEntity<IndicatorFillResponse> fillBb(@Valid @RequestBody IndicatorFillRequest request) {
        IndicatorOperationResult result = indicatorService.fillBb(request.getExchangeInstrumentId(),
                CanonicalTimeframe.fromCode(request.getTimeframe()), request.getParamsRef());
        IndicatorFillResponse response = buildFillResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping(Constants.Api.INDICATOR_FILL_OBV_PATH)
    @Operation(summary = "Расчёт значений OBV")
    public ResponseEntity<IndicatorFillResponse> fillObv(@Valid @RequestBody IndicatorFillRequest request) {
        IndicatorOperationResult result = indicatorService.fillObv(request.getExchangeInstrumentId(),
                CanonicalTimeframe.fromCode(request.getTimeframe()), request.getParamsRef());
        IndicatorFillResponse response = buildFillResponse(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping(Constants.Api.INDICATOR_CHECKPOINT_PATH)
    @Operation(summary = "Получение чекпоинта индикатора")
    public ResponseEntity<IndicatorCheckpointResponse> getCheckpoint(@RequestParam String indicator,
            @RequestParam String timeframe, @RequestParam UUID exchangeInstrumentId, @RequestParam Long paramsRef) {
        IndicatorType indicatorType = IndicatorType.valueOf(indicator.toUpperCase());
        CanonicalTimeframe canonicalTimeframe = CanonicalTimeframe.fromCode(timeframe);
        Optional<IndicatorCheckpoint> checkpoint = indicatorService.findCheckpoint(indicatorType, canonicalTimeframe,
                exchangeInstrumentId, paramsRef);
        IndicatorCheckpointResponse response = checkpoint.map(value -> IndicatorCheckpointResponse.builder()
            .indicator(value.getIndicator().name())
            .timeframe(value.getTimeframe().name())
            .exchangeInstrumentId(value.getExchangeInstrumentId())
            .paramsRef(value.getParamsRef())
            .version(value.getVersion())
            .lastTimestamp(value.getLastTimestamp())
            .updatedAt(value.getUpdatedAt())
            .build()).orElseGet(() -> IndicatorCheckpointResponse.builder()
            .indicator(indicatorType.name())
            .timeframe(canonicalTimeframe.name())
            .exchangeInstrumentId(exchangeInstrumentId)
            .paramsRef(paramsRef)
            .build());
        return ResponseEntity.ok(response);
    }

    private IndicatorFillResponse buildFillResponse(IndicatorOperationResult result) {
        return IndicatorFillResponse.builder()
            .exchangeInstrumentId(result.getExchangeInstrumentId())
            .timeframe(result.getTimeframe().name())
            .paramsRef(result.getParamsRef())
            .indicator(result.getIndicator().name())
            .insertedValues(result.getInsertedValues())
            .warmupSkipped(result.getWarmupSkipped())
            .message(Constants.Messages.INDICATOR_FILL_ACCEPTED)
            .build();
    }
}
