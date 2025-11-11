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

import com.example.vibetradingbot.api.v1.dto.QuorumCheckpointResponse;
import com.example.vibetradingbot.api.v1.dto.QuorumDecideRangeRequest;
import com.example.vibetradingbot.api.v1.dto.QuorumDecideRequest;
import com.example.vibetradingbot.api.v1.dto.QuorumDecisionDto;
import com.example.vibetradingbot.api.v1.dto.QuorumLatestResponse;
import com.example.vibetradingbot.api.v1.dto.QuorumOperationResponse;
import com.example.vibetradingbot.api.v1.dto.QuorumParamsActivateRequest;
import com.example.vibetradingbot.api.v1.dto.QuorumParamsActivateResponse;
import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.enums.Direction;
import com.example.vibetradingbot.domain.enums.QuorumAction;
import com.example.vibetradingbot.domain.model.QuorumCheckpoint;
import com.example.vibetradingbot.domain.model.QuorumDecisionEvent;
import com.example.vibetradingbot.domain.model.QuorumOperationResult;
import com.example.vibetradingbot.domain.model.QuorumParamsActivationResult;
import com.example.vibetradingbot.domain.model.QuorumReason;
import com.example.vibetradingbot.service.QuorumService;
import com.example.vibetradingbot.util.Constants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Контроллер API кворумного движка.
 */
@RestController
@Tag(name = "Quorum")
public class QuorumController {

    private static final int DEFAULT_LIMIT = 50;

    private final QuorumService quorumService;

    public QuorumController(QuorumService quorumService) {
        this.quorumService = quorumService;
    }

    @PostMapping(Constants.Api.QUORUM_DECIDE_PATH)
    @Operation(summary = "Инкрементальное решение кворума")
    public ResponseEntity<QuorumOperationResponse> decide(@Valid @RequestBody QuorumDecideRequest request) {
        CanonicalTimeframe timeframe = CanonicalTimeframe.fromCode(request.getTimeframe());
        QuorumOperationResult result = quorumService.decide(request.getExchangeInstrumentId(), timeframe,
                request.getQuorumParamsRef());
        QuorumOperationResponse response = QuorumOperationResponse.builder()
            .exchangeInstrumentId(result.getExchangeInstrumentId())
            .timeframe(result.getTimeframe().name())
            .quorumParamsRef(result.getQuorumParamsRef())
            .insertedDecisions(result.getInsertedDecisions())
            .skippedByDebounce(result.getSkippedByDebounce())
            .skippedByCooldown(result.getSkippedByCooldown())
            .message(Constants.Messages.QUORUM_DECISION_ACCEPTED)
            .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping(Constants.Api.QUORUM_DECIDE_RANGE_PATH)
    @Operation(summary = "Пакетное выполнение кворума в диапазоне")
    public ResponseEntity<QuorumOperationResponse> decideRange(@Valid @RequestBody QuorumDecideRangeRequest request) {
        CanonicalTimeframe timeframe = CanonicalTimeframe.fromCode(request.getTimeframe());
        QuorumOperationResult result = quorumService.decideRange(request.getExchangeInstrumentId(), timeframe,
                request.getQuorumParamsRef(), request.getFromUtc(), request.getToUtc());
        QuorumOperationResponse response = QuorumOperationResponse.builder()
            .exchangeInstrumentId(result.getExchangeInstrumentId())
            .timeframe(result.getTimeframe().name())
            .quorumParamsRef(result.getQuorumParamsRef())
            .insertedDecisions(result.getInsertedDecisions())
            .skippedByDebounce(result.getSkippedByDebounce())
            .skippedByCooldown(result.getSkippedByCooldown())
            .message(Constants.Messages.QUORUM_DECISION_ACCEPTED)
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping(Constants.Api.QUORUM_LATEST_PATH)
    @Operation(summary = "Получить последние решения кворума")
    public ResponseEntity<QuorumLatestResponse> latest(@RequestParam UUID exchangeInstrumentId,
            @RequestParam String timeframe, @RequestParam(required = false) Integer limit) {
        int resolvedLimit = Objects.nonNull(limit) && limit > 0 ? limit : DEFAULT_LIMIT;
        CanonicalTimeframe canonicalTimeframe = CanonicalTimeframe.fromCode(timeframe);
        List<QuorumDecisionEvent> events = quorumService.findLatestDecisions(exchangeInstrumentId, canonicalTimeframe,
                resolvedLimit);
        List<QuorumDecisionDto> payload = new ArrayList<>();
        for (QuorumDecisionEvent event : events) {
            payload.add(mapEvent(event));
        }
        QuorumLatestResponse response = QuorumLatestResponse.builder()
            .decisions(payload)
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping(Constants.Api.QUORUM_CHECKPOINT_PATH)
    @Operation(summary = "Получить чекпоинт кворума")
    public ResponseEntity<QuorumCheckpointResponse> checkpoint(@RequestParam UUID exchangeInstrumentId,
            @RequestParam String timeframe, @RequestParam Long quorumParamsRef) {
        CanonicalTimeframe canonicalTimeframe = CanonicalTimeframe.fromCode(timeframe);
        Optional<QuorumCheckpoint> checkpoint = quorumService.findCheckpoint(exchangeInstrumentId, canonicalTimeframe,
                quorumParamsRef);
        QuorumCheckpointResponse response = checkpoint.map(value -> QuorumCheckpointResponse.builder()
            .quorumParamsId(value.getQuorumParamsId())
            .version(value.getVersion())
            .exchangeInstrumentId(value.getExchangeInstrumentId())
            .timeframe(value.getTimeframe().name())
            .lastTimestamp(value.getLastTimestamp())
            .updatedAt(value.getUpdatedAt())
            .build()).orElseGet(() -> QuorumCheckpointResponse.builder()
            .quorumParamsId(quorumParamsRef)
            .version(0)
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(canonicalTimeframe.name())
            .build());
        return ResponseEntity.ok(response);
    }

    @PostMapping(Constants.Api.QUORUM_PARAMS_ACTIVATE_PATH)
    @Operation(summary = "Активировать параметры кворума")
    public ResponseEntity<QuorumParamsActivateResponse> activate(@Valid @RequestBody QuorumParamsActivateRequest request) {
        QuorumParamsActivationResult result = quorumService.activateParams(request.getVersion(),
                request.getConfig().toString());
        QuorumParamsActivateResponse response = QuorumParamsActivateResponse.builder()
            .quorumParamsId(result.getQuorumParamsId())
            .version(result.getVersion())
            .build();
        return ResponseEntity.ok(response);
    }

    private QuorumDecisionDto mapEvent(QuorumDecisionEvent event) {
        Map<String, Object> reason = mapReason(event.getReason());
        return QuorumDecisionDto.builder()
            .id(event.getId())
            .exchangeInstrumentId(event.getExchangeInstrumentId())
            .timeframe(event.getTimeframe().name())
            .timestamp(event.getTimestamp())
            .action(event.getAction().name())
            .direction(Objects.isNull(event.getDirection()) ? null : event.getDirection().name())
            .score(event.getScore())
            .thresholdEnter(event.getThresholdEnter())
            .thresholdExit(event.getThresholdExit())
            .quorumParamsId(event.getQuorumParamsId())
            .version(event.getVersion())
            .positionState(event.getPositionState())
            .reason(reason)
            .createdAt(event.getCreatedAt())
            .build();
    }

    private Map<String, Object> mapReason(QuorumReason reason) {
        if (Objects.isNull(reason)) {
            return Map.of();
        }
        Map<String, Object> payload = new HashMap<>();
        List<Map<String, Object>> inputs = new ArrayList<>();
        for (QuorumReason.Input input : reason.getInputs()) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", input.getType());
            item.put("tf", input.getTimeframe());
            item.put("score", input.getScore());
            item.put("weight", input.getWeight());
            item.put("contrib", input.getContribution());
            item.put("fields", input.getFields());
            inputs.add(item);
        }
        payload.put("inputs", inputs);
        List<Map<String, Object>> filters = new ArrayList<>();
        for (QuorumReason.Filter filter : reason.getFilters()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", filter.getName());
            item.put("mode", filter.getMode());
            item.put("passed", filter.isPassed());
            if (Objects.nonNull(filter.getFactor())) {
                item.put("factor", filter.getFactor());
            }
            item.put("detail", filter.getDetail());
            filters.add(item);
        }
        payload.put("filters", filters);
        QuorumReason.Aggregation aggregation = reason.getAggregation();
        if (Objects.nonNull(aggregation)) {
            Map<String, Object> aggregationMap = new HashMap<>();
            aggregationMap.put("rawScore", aggregation.getRawScore());
            aggregationMap.put("afterFilters", aggregation.getAfterFilters());
            QuorumReason.Thresholds thresholds = aggregation.getThresholds();
            if (Objects.nonNull(thresholds)) {
                Map<String, Object> thresholdMap = new HashMap<>();
                thresholdMap.put("enter", thresholds.getEnter());
                thresholdMap.put("exit", thresholds.getExit());
                aggregationMap.put("thresholds", thresholdMap);
            }
            payload.put("aggregation", aggregationMap);
        }
        payload.put("action", reason.getAction() != null ? reason.getAction().name() : QuorumAction.HOLD.name());
        Direction direction = reason.getDirection();
        if (Objects.nonNull(direction)) {
            payload.put("direction", direction.name());
        }
        payload.put("notes", reason.getNotes());
        return payload;
    }
}
