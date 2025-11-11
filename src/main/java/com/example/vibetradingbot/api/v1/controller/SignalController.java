package com.example.vibetradingbot.api.v1.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vibetradingbot.api.v1.dto.SignalCheckpointResponse;
import com.example.vibetradingbot.api.v1.dto.SignalEventDto;
import com.example.vibetradingbot.api.v1.dto.SignalGenerateRangeRequest;
import com.example.vibetradingbot.api.v1.dto.SignalGenerateRequest;
import com.example.vibetradingbot.api.v1.dto.SignalLatestResponse;
import com.example.vibetradingbot.api.v1.dto.SignalOperationResponse;
import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.enums.SignalDirection;
import com.example.vibetradingbot.domain.enums.SignalType;
import com.example.vibetradingbot.domain.model.SignalCheckpoint;
import com.example.vibetradingbot.domain.model.SignalEvent;
import com.example.vibetradingbot.domain.model.SignalReason;
import com.example.vibetradingbot.domain.model.SignalOperationResult;
import com.example.vibetradingbot.service.SignalService;
import com.example.vibetradingbot.util.Constants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Контроллер API для работы с генерацией сигналов.
 */
@RestController
@Tag(name = "Signals")
public class SignalController {

    private static final int DEFAULT_LIMIT = 20;

    private final SignalService signalService;

    public SignalController(SignalService signalService) {
        this.signalService = signalService;
    }

    @PostMapping(Constants.Api.SIGNALS_GENERATE_PATH)
    @Operation(summary = "Инкрементальная генерация сигналов")
    public ResponseEntity<SignalOperationResponse> generate(@Valid @RequestBody SignalGenerateRequest request) {
        SignalOperationResult result = signalService.generate(request.getExchangeInstrumentId(),
                CanonicalTimeframe.fromCode(request.getTimeframe()),
                SignalType.valueOf(request.getSignalType().toUpperCase()), request.getParamsRef());
        SignalOperationResponse response = buildResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping(Constants.Api.SIGNALS_GENERATE_RANGE_PATH)
    @Operation(summary = "Генерация сигналов в диапазоне времени")
    public ResponseEntity<SignalOperationResponse> generateRange(@Valid @RequestBody SignalGenerateRangeRequest request) {
        SignalOperationResult result = signalService.generateRange(request.getExchangeInstrumentId(),
                CanonicalTimeframe.fromCode(request.getTimeframe()),
                SignalType.valueOf(request.getSignalType().toUpperCase()), request.getParamsRef(), request.getFromUtc(),
                request.getToUtc());
        SignalOperationResponse response = buildResponse(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping(Constants.Api.SIGNALS_LATEST_PATH)
    @Operation(summary = "Получить последние сигналы")
    public ResponseEntity<SignalLatestResponse> latest(@RequestParam UUID exchangeInstrumentId,
            @RequestParam String timeframe, @RequestParam String signalType,
            @RequestParam(required = false) Integer limit) {
        int resolvedLimit = Objects.nonNull(limit) && limit > 0 ? limit : DEFAULT_LIMIT;
        List<SignalEvent> events = signalService.findLatestSignals(exchangeInstrumentId,
                CanonicalTimeframe.fromCode(timeframe), SignalType.valueOf(signalType.toUpperCase()), resolvedLimit);
        List<SignalEventDto> dtos = new ArrayList<>();
        for (SignalEvent event : events) {
            dtos.add(mapEvent(event));
        }
        SignalLatestResponse response = SignalLatestResponse.builder()
            .signals(dtos)
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping(Constants.Api.SIGNALS_CHECKPOINT_PATH)
    @Operation(summary = "Получить чекпоинт генерации сигнала")
    public ResponseEntity<SignalCheckpointResponse> checkpoint(@RequestParam String signalType,
            @RequestParam String timeframe, @RequestParam UUID exchangeInstrumentId, @RequestParam Long paramsRef) {
        SignalType type = SignalType.valueOf(signalType.toUpperCase());
        CanonicalTimeframe canonicalTimeframe = CanonicalTimeframe.fromCode(timeframe);
        Optional<SignalCheckpoint> checkpoint = signalService.findCheckpoint(type, canonicalTimeframe, exchangeInstrumentId,
                paramsRef);
        SignalCheckpointResponse response = checkpoint.map(value -> SignalCheckpointResponse.builder()
            .signalType(value.getSignalType().name())
            .timeframe(value.getTimeframe().name())
            .exchangeInstrumentId(value.getExchangeInstrumentId())
            .paramsRef(value.getParamsRef())
            .version(value.getVersion())
            .lastTimestamp(value.getLastTimestamp())
            .updatedAt(value.getUpdatedAt())
            .build()).orElseGet(() -> SignalCheckpointResponse.builder()
            .signalType(type.name())
            .timeframe(canonicalTimeframe.name())
            .exchangeInstrumentId(exchangeInstrumentId)
            .paramsRef(paramsRef)
            .build());
        return ResponseEntity.ok(response);
    }

    private SignalOperationResponse buildResponse(SignalOperationResult result) {
        return SignalOperationResponse.builder()
            .exchangeInstrumentId(result.getExchangeInstrumentId())
            .timeframe(result.getTimeframe().name())
            .signalType(result.getSignalType().name())
            .paramsRef(result.getParamsRef())
            .insertedSignals(result.getInsertedSignals())
            .skippedByDebounce(result.getSkippedByDebounce())
            .skippedByCooldown(result.getSkippedByCooldown())
            .message(Constants.Messages.SIGNAL_GENERATION_ACCEPTED)
            .build();
    }

    private SignalEventDto mapEvent(SignalEvent event) {
        Map<String, Object> reason = mapReason(event.getReason());
        return SignalEventDto.builder()
            .id(event.getId())
            .exchangeInstrumentId(event.getExchangeInstrumentId())
            .timeframe(event.getTimeframe().name())
            .timestamp(event.getTimestamp())
            .signalType(event.getSignalType().name())
            .direction(event.getDirection().name())
            .score(event.getScore())
            .reason(reason)
            .paramsRef(event.getParamsRef())
            .version(event.getVersion())
            .createdAt(event.getCreatedAt())
            .build();
    }

    private Map<String, Object> mapReason(SignalReason reason) {
        if (Objects.isNull(reason)) {
            return Map.of();
        }
        Map<String, Object> payload = new HashMap<>();
        List<Map<String, Object>> inputs = new ArrayList<>();
        for (SignalReason.Input input : reason.getInputs()) {
            Map<String, Object> item = new HashMap<>();
            item.put("indicator", input.getIndicator());
            item.put("tf", input.getTimeframe());
            item.put("fields", input.getFields());
            item.put("weight", input.getWeight());
            item.put("contrib", input.getContribution());
            inputs.add(item);
        }
        payload.put("inputs", inputs);
        List<Map<String, Object>> filters = new ArrayList<>();
        for (SignalReason.Filter filter : reason.getFilters()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", filter.getName());
            item.put("passed", filter.isPassed());
            item.put("detail", filter.getDetail());
            filters.add(item);
        }
        payload.put("filters", filters);
        payload.put("score", reason.getScore());
        SignalDirection direction = reason.getDirection();
        if (Objects.nonNull(direction)) {
            payload.put("direction", direction.name());
        }
        payload.put("notes", reason.getNotes());
        return payload;
    }
}
