package com.example.vibetradingbot.api.v1.controller;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vibetradingbot.api.v1.dto.HealthResponse;
import com.example.vibetradingbot.api.v1.dto.LiveCancelRequest;
import com.example.vibetradingbot.api.v1.dto.LiveEventDto;
import com.example.vibetradingbot.api.v1.dto.LiveEventsResponse;
import com.example.vibetradingbot.api.v1.dto.LiveModifyRequest;
import com.example.vibetradingbot.api.v1.dto.LiveOrderDto;
import com.example.vibetradingbot.api.v1.dto.LiveOrderRequest;
import com.example.vibetradingbot.api.v1.dto.LiveOrdersResponse;
import com.example.vibetradingbot.api.v1.dto.LivePositionDto;
import com.example.vibetradingbot.api.v1.dto.LivePositionsResponse;
import com.example.vibetradingbot.api.v1.dto.RiskHaltRequest;
import com.example.vibetradingbot.api.v1.dto.RiskHaltStateDto;
import com.example.vibetradingbot.api.v1.dto.RiskHaltStateResponse;
import com.example.vibetradingbot.api.v1.dto.RiskResumeRequest;
import com.example.vibetradingbot.api.v1.mapper.LiveMapper;
import com.example.vibetradingbot.config.ExecutionProperties;
import com.example.vibetradingbot.domain.enums.OrderStatus;
import com.example.vibetradingbot.domain.enums.OrderTimeInForce;
import com.example.vibetradingbot.domain.enums.OrderType;
import com.example.vibetradingbot.domain.model.CancelOrderCommand;
import com.example.vibetradingbot.domain.model.ModifyOrderCommand;
import com.example.vibetradingbot.domain.model.NewOrder;
import com.example.vibetradingbot.domain.model.OrderSnapshot;
import com.example.vibetradingbot.domain.model.PositionSnapshot;
import com.example.vibetradingbot.exception.ServiceException;
import com.example.vibetradingbot.service.LiveTradingService;
import com.example.vibetradingbot.service.ReconcileService;
import com.example.vibetradingbot.util.Constants;

/**
 * REST-контроллер управления live-торговлей.
 */
@RestController
@RequestMapping(Constants.Api.LIVE_BASE_PATH)
@Validated
public class LiveTradingController {

    private final LiveTradingService liveTradingService;
    private final ReconcileService reconcileService;
    private final LiveMapper liveMapper;
    private final ExecutionProperties executionProperties;

    public LiveTradingController(LiveTradingService liveTradingService,
        ReconcileService reconcileService,
        LiveMapper liveMapper,
        ExecutionProperties executionProperties) {
        this.liveTradingService = Objects.requireNonNull(liveTradingService, Constants.Validation.NULL_IDENTIFIER);
        this.reconcileService = Objects.requireNonNull(reconcileService, Constants.Validation.NULL_IDENTIFIER);
        this.liveMapper = Objects.requireNonNull(liveMapper, Constants.Validation.NULL_IDENTIFIER);
        this.executionProperties = Objects.requireNonNull(executionProperties, Constants.Validation.NULL_IDENTIFIER);
    }

    @PostMapping("/place")
    public ResponseEntity<LiveOrdersResponse> placeOrder(@Valid @RequestBody LiveOrderRequest request) {
        NewOrder newOrder = NewOrder.create(null,
            request.getExchangeInstrumentId(),
            request.getSide(),
            Optional.ofNullable(request.getType()).orElse(OrderType.MARKET),
            request.getQuantity(),
            request.getPrice(),
            request.getStopLoss(),
            request.getTakeProfit(),
            Optional.ofNullable(request.getTimeInForce()).orElse(OrderTimeInForce.GTC),
            Instant.now());
        var ack = liveTradingService.placeOrder(newOrder);
        if (!ack.isAccepted()) {
            throw new ServiceException(Constants.Errors.LIVE_ORDER_REJECTED, ack.getMessage());
        }
        Optional<OrderSnapshot> snapshot = liveTradingService.getOrder(ack.getClientOrderId(), ack.getExchangeOrderId());
        List<LiveOrderDto> orders = snapshot.map(value -> Collections.singletonList(liveMapper.toDto(value)))
            .orElse(Collections.emptyList());
        return ResponseEntity.ok(LiveOrdersResponse.builder().orders(orders).build());
    }

    @PostMapping("/cancel")
    public ResponseEntity<LiveOrdersResponse> cancelOrder(@Valid @RequestBody LiveCancelRequest request) {
        if (StringUtils.isBlank(request.getClientOrderId()) && StringUtils.isBlank(request.getExchangeOrderId())) {
            throw new ServiceException(Constants.Errors.INVALID_CLIENT_PAYLOAD, Constants.Errors.INVALID_CLIENT_PAYLOAD);
        }
        CancelOrderCommand command = StringUtils.isNotBlank(request.getClientOrderId())
            ? CancelOrderCommand.byClientOrderId(request.getClientOrderId(), Instant.now())
            : CancelOrderCommand.byExchangeOrderId(request.getExchangeOrderId(), Instant.now());
        var ack = liveTradingService.cancelOrder(command);
        Optional<OrderSnapshot> snapshot = liveTradingService.getOrder(command.getClientOrderId(), command.getExchangeOrderId());
        List<LiveOrderDto> orders = snapshot.map(value -> Collections.singletonList(liveMapper.toDto(value)))
            .orElse(Collections.emptyList());
        return ResponseEntity.ok(LiveOrdersResponse.builder().orders(orders).build());
    }

    @PostMapping("/modify")
    public ResponseEntity<LiveOrdersResponse> modifyOrder(@Valid @RequestBody LiveModifyRequest request) {
        if (Objects.isNull(request.getNewPrice()) && Objects.isNull(request.getNewStopLoss()) && Objects.isNull(request.getNewTakeProfit())) {
            throw new ServiceException(Constants.Errors.INVALID_CLIENT_PAYLOAD, Constants.Errors.INVALID_CLIENT_PAYLOAD);
        }
        ModifyOrderCommand command = ModifyOrderCommand.create(request.getClientOrderId(),
            request.getNewPrice(),
            request.getNewStopLoss(),
            request.getNewTakeProfit(),
            Instant.now());
        var ack = liveTradingService.modifyOrder(command);
        Optional<OrderSnapshot> snapshot = liveTradingService.getOrder(request.getClientOrderId(), null);
        List<LiveOrderDto> orders = snapshot.map(value -> Collections.singletonList(liveMapper.toDto(value)))
            .orElse(Collections.emptyList());
        return ResponseEntity.ok(LiveOrdersResponse.builder().orders(orders).build());
    }

    @GetMapping("/orders")
    public ResponseEntity<LiveOrdersResponse> listOrders(@RequestParam(name = "status", required = false) OrderStatus status,
        @RequestParam(name = "exchangeInstrumentId", required = false) Long exchangeInstrumentId) {
        List<OrderSnapshot> snapshots = liveTradingService.listOpenOrders(exchangeInstrumentId);
        if (Objects.nonNull(status)) {
            snapshots = snapshots.stream().filter(order -> order.getStatus() == status).toList();
        }
        List<LiveOrderDto> orders = liveMapper.toOrderDtos(snapshots);
        return ResponseEntity.ok(LiveOrdersResponse.builder().orders(orders).build());
    }

    @GetMapping("/positions")
    public ResponseEntity<LivePositionsResponse> listPositions() {
        List<PositionSnapshot> snapshots = liveTradingService.listPositions();
        List<LivePositionDto> positions = liveMapper.toPositionDtos(snapshots);
        return ResponseEntity.ok(LivePositionsResponse.builder().positions(positions).build());
    }

    @GetMapping("/events")
    public ResponseEntity<LiveEventsResponse> listEvents(@RequestParam(name = "limit", defaultValue = "100") int limit) {
        List<LiveEventDto> events = liveMapper.toEventDtos(liveTradingService.listEvents(limit));
        return ResponseEntity.ok(LiveEventsResponse.builder().events(events).build());
    }

    @PostMapping("/halt")
    public ResponseEntity<RiskHaltStateResponse> halt(@Valid @RequestBody RiskHaltRequest request) {
        liveTradingService.halt(request.getScope(), request.getExchangeInstrumentId(), request.getReason());
        List<RiskHaltStateDto> halts = liveMapper.toHaltDtos(liveTradingService.getHalts());
        return ResponseEntity.ok(RiskHaltStateResponse.builder().halts(halts).build());
    }

    @PostMapping("/resume")
    public ResponseEntity<RiskHaltStateResponse> resume(@Valid @RequestBody RiskResumeRequest request) {
        liveTradingService.resume(request.getScope(), request.getExchangeInstrumentId());
        List<RiskHaltStateDto> halts = liveMapper.toHaltDtos(liveTradingService.getHalts());
        return ResponseEntity.ok(RiskHaltStateResponse.builder().halts(halts).build());
    }

    @GetMapping("/halt/state")
    public ResponseEntity<RiskHaltStateResponse> haltState() {
        List<RiskHaltStateDto> halts = liveMapper.toHaltDtos(liveTradingService.getHalts());
        return ResponseEntity.ok(RiskHaltStateResponse.builder().halts(halts).build());
    }

    @PostMapping("/reconcile/run")
    public ResponseEntity<HealthResponse> reconcile() {
        reconcileService.reconcileNow();
        return ResponseEntity.ok(HealthResponse.builder()
            .status(Constants.Messages.HEALTH_STATUS_UP)
            .message(Constants.Messages.LIVE_RECONCILE_TRIGGERED)
            .timestamp(Instant.now())
            .build());
    }

    @GetMapping("/connector/health")
    public ResponseEntity<HealthResponse> connectorHealth() {
        return ResponseEntity.ok(HealthResponse.builder()
            .status(Constants.Messages.HEALTH_STATUS_UP)
            .message(executionProperties.getMode().name())
            .timestamp(Instant.now())
            .build());
    }
}
