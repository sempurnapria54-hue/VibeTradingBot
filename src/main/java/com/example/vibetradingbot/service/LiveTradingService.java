package com.example.vibetradingbot.service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.vibetradingbot.config.ExecutionProperties;
import com.example.vibetradingbot.config.TrailingProperties;
import com.example.vibetradingbot.domain.enums.OrderEventType;
import com.example.vibetradingbot.domain.enums.OrderStatus;
import com.example.vibetradingbot.domain.enums.OrderTimeInForce;
import com.example.vibetradingbot.domain.enums.OrderType;
import com.example.vibetradingbot.domain.enums.RiskHaltScope;
import com.example.vibetradingbot.domain.model.CancelAck;
import com.example.vibetradingbot.domain.model.CancelOrderCommand;
import com.example.vibetradingbot.domain.model.ModifyAck;
import com.example.vibetradingbot.domain.model.ModifyOrderCommand;
import com.example.vibetradingbot.domain.model.NewOrder;
import com.example.vibetradingbot.domain.model.OrderAck;
import com.example.vibetradingbot.domain.model.OrderEvent;
import com.example.vibetradingbot.domain.model.OrderSnapshot;
import com.example.vibetradingbot.domain.model.PositionSnapshot;
import com.example.vibetradingbot.domain.model.RiskCheckResult;
import com.example.vibetradingbot.domain.model.RiskHaltState;
import com.example.vibetradingbot.exchange.connector.ExecutionAdapter;
import com.example.vibetradingbot.exception.ServiceException;
import com.example.vibetradingbot.util.Constants;

/**
 * Сервис live-торговли, объединяющий риск-контроль и исполнение.
 */
@Service
public class LiveTradingService {

    private static final int MAX_EVENTS = 500;

    private final ExecutionAdapter executionAdapter;
    private final RiskService riskService;
    private final ExecutionProperties executionProperties;
    private final TrailingProperties trailingProperties;
    private final Deque<OrderEvent> orderEvents;

    public LiveTradingService(ExecutionAdapter executionAdapter,
        RiskService riskService,
        ExecutionProperties executionProperties,
        TrailingProperties trailingProperties) {
        this.executionAdapter = Objects.requireNonNull(executionAdapter, Constants.Validation.NULL_IDENTIFIER);
        this.riskService = Objects.requireNonNull(riskService, Constants.Validation.NULL_IDENTIFIER);
        this.executionProperties = Objects.requireNonNull(executionProperties, Constants.Validation.NULL_IDENTIFIER);
        this.trailingProperties = Objects.requireNonNull(trailingProperties, Constants.Validation.NULL_IDENTIFIER);
        this.orderEvents = new ArrayDeque<>();
    }

    public OrderAck placeOrder(NewOrder newOrder) {
        Objects.requireNonNull(newOrder, Constants.Validation.NULL_IDENTIFIER);
        NewOrder normalizedOrder = normalizeOrder(newOrder);
        RiskCheckResult riskResult = riskService.preCheck(normalizedOrder);
        if (!riskResult.isAllowed()) {
            throw new ServiceException(Constants.Errors.RISK_VIOLATION, riskResult.getReason());
        }
        registerEvent(normalizedOrder.getClientOrderId(), null, OrderEventType.REQUESTED, executionProperties.getMode().name());
        OrderAck ack = executionAdapter.createOrder(normalizedOrder);
        OrderEventType ackType = ack.getStatus() == OrderStatus.REJECTED ? OrderEventType.REJECTED : OrderEventType.SUBMITTED;
        registerEvent(ack.getClientOrderId(), ack.getExchangeOrderId(), ackType, trailingProperties.isEnabled() ? "trailing" : "static");
        return ack;
    }

    public CancelAck cancelOrder(CancelOrderCommand cancelOrderCommand) {
        Objects.requireNonNull(cancelOrderCommand, Constants.Validation.NULL_IDENTIFIER);
        CancelAck ack = executionAdapter.cancelOrder(cancelOrderCommand);
        registerEvent(cancelOrderCommand.getClientOrderId(), null, OrderEventType.CANCELED, Constants.Messages.LIVE_ORDER_CANCEL_ACCEPTED);
        if (!ack.isAccepted()) {
            throw new ServiceException(Constants.Errors.LIVE_ORDER_NOT_FOUND, ack.getMessage());
        }
        return ack;
    }

    public ModifyAck modifyOrder(ModifyOrderCommand modifyOrderCommand) {
        Objects.requireNonNull(modifyOrderCommand, Constants.Validation.NULL_IDENTIFIER);
        ModifyAck ack = executionAdapter.modifyOrder(modifyOrderCommand);
        registerEvent(modifyOrderCommand.getClientOrderId(), null, OrderEventType.MODIFIED, Constants.Messages.LIVE_ORDER_MODIFY_ACCEPTED);
        if (!ack.isAccepted()) {
            throw new ServiceException(Constants.Errors.LIVE_ORDER_NOT_FOUND, ack.getMessage());
        }
        return ack;
    }

    public Optional<OrderSnapshot> getOrder(String clientOrderId, String exchangeOrderId) {
        return executionAdapter.getOrder(clientOrderId, exchangeOrderId);
    }

    public List<OrderSnapshot> listOpenOrders(Long exchangeInstrumentId) {
        return executionAdapter.listOpenOrders(exchangeInstrumentId);
    }

    public List<PositionSnapshot> listPositions() {
        return executionAdapter.getPositions();
    }

    public List<OrderEvent> listEvents(int limit) {
        int effectiveLimit = Math.max(1, Math.min(limit, MAX_EVENTS));
        return orderEvents.stream()
            .limit(effectiveLimit)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public void halt(RiskHaltScope scope, Long exchangeInstrumentId, String reason) {
        riskService.haltTrading(scope, exchangeInstrumentId, reason);
        registerEvent(String.valueOf(UUID.randomUUID()), null, OrderEventType.CANCELED, reason);
    }

    public void resume(RiskHaltScope scope, Long exchangeInstrumentId) {
        riskService.resumeTrading(scope, exchangeInstrumentId);
        registerEvent(String.valueOf(UUID.randomUUID()), null, OrderEventType.ACCEPTED, Constants.Messages.LIVE_HALT_DISABLED);
    }

    public List<RiskHaltState> getHalts() {
        return riskService.getHalts();
    }

    private NewOrder normalizeOrder(NewOrder newOrder) {
        String clientOrderId = Objects.nonNull(newOrder.getClientOrderId()) ? newOrder.getClientOrderId() : generateClientOrderId();
        OrderTimeInForce timeInForce = Objects.nonNull(newOrder.getTimeInForce()) ? newOrder.getTimeInForce() : OrderTimeInForce.GTC;
        OrderType type = Objects.nonNull(newOrder.getType()) ? newOrder.getType() : OrderType.MARKET;
        Instant createdAt = Objects.nonNull(newOrder.getCreatedAt()) ? newOrder.getCreatedAt() : Instant.now();
        return NewOrder.create(clientOrderId,
            newOrder.getExchangeInstrumentId(),
            newOrder.getSide(),
            type,
            newOrder.getQuantity(),
            newOrder.getPrice(),
            newOrder.getStopLoss(),
            newOrder.getTakeProfit(),
            timeInForce,
            createdAt);
    }

    private void registerEvent(String clientOrderId, String exchangeOrderId, OrderEventType eventType, String payload) {
        OrderEvent event = OrderEvent.create(clientOrderId, exchangeOrderId, eventType, payload, Instant.now());
        if (orderEvents.size() >= MAX_EVENTS) {
            orderEvents.removeFirst();
        }
        orderEvents.addLast(event);
    }

    private String generateClientOrderId() {
        return UUID.randomUUID().toString();
    }
}
