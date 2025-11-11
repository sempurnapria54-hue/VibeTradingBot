package com.example.vibetradingbot.exchange.connector;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.vibetradingbot.config.ExecutionProperties;
import com.example.vibetradingbot.domain.enums.OrderSide;
import com.example.vibetradingbot.domain.enums.OrderStatus;
import com.example.vibetradingbot.domain.enums.OrderType;
import com.example.vibetradingbot.domain.model.CancelAck;
import com.example.vibetradingbot.domain.model.CancelOrderCommand;
import com.example.vibetradingbot.domain.model.ModifyAck;
import com.example.vibetradingbot.domain.model.ModifyOrderCommand;
import com.example.vibetradingbot.domain.model.NewOrder;
import com.example.vibetradingbot.domain.model.OrderAck;
import com.example.vibetradingbot.domain.model.OrderSnapshot;
import com.example.vibetradingbot.domain.model.PositionSnapshot;
import com.example.vibetradingbot.util.Constants;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Упрощённый адаптер исполнения для OKX.
 */
@Component
public class OkxExecutionAdapter implements ExecutionAdapter {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final ExecutionProperties executionProperties;
    private final MeterRegistry meterRegistry;
    private final Map<String, OrderSnapshot> orders;
    private final Map<Long, PositionSnapshot> positions;
    private final AtomicLong exchangeSequence;

    public OkxExecutionAdapter(ExecutionProperties executionProperties, MeterRegistry meterRegistry) {
        this.executionProperties = Objects.requireNonNull(executionProperties, Constants.Validation.NULL_IDENTIFIER);
        this.meterRegistry = Objects.requireNonNull(meterRegistry, Constants.Validation.NULL_IDENTIFIER);
        this.orders = new ConcurrentHashMap<>();
        this.positions = new ConcurrentHashMap<>();
        this.exchangeSequence = new AtomicLong(1L);
    }

    @Override
    public OrderAck createOrder(NewOrder newOrder) {
        Objects.requireNonNull(newOrder, Constants.Validation.NULL_IDENTIFIER);
        meterRegistry.counter(Constants.Metrics.LIVE_ORDER_REQUESTED).increment();
        meterRegistry.counter(Constants.Metrics.LIVE_EXCHANGE_CALLS).increment();
        Instant now = Instant.now();
        String exchangeOrderId = buildExchangeOrderId();
        OrderStatus status = resolveInitialStatus(newOrder.getType());
        BigDecimal filledQty = status == OrderStatus.FILLED ? newOrder.getQuantity() : ZERO;
        BigDecimal averagePrice = status == OrderStatus.FILLED ? deriveAveragePrice(newOrder) : null;
        OrderSnapshot snapshot = OrderSnapshot.create(newOrder.getClientOrderId(),
            exchangeOrderId,
            newOrder.getExchangeInstrumentId(),
            newOrder.getSide(),
            newOrder.getType(),
            newOrder.getTimeInForce(),
            newOrder.getQuantity(),
            newOrder.getPrice(),
            status,
            filledQty,
            averagePrice,
            newOrder.getCreatedAt(),
            now);
        orders.put(newOrder.getClientOrderId(), snapshot);
        meterRegistry.counter(Constants.Metrics.LIVE_ORDER_SUBMITTED).increment();
        if (status == OrderStatus.FILLED) {
            meterRegistry.counter(Constants.Metrics.LIVE_ORDER_FILLED).increment();
            updatePosition(snapshot);
        }
        return OrderAck.accepted(newOrder.getClientOrderId(), exchangeOrderId, status, now);
    }

    @Override
    public CancelAck cancelOrder(CancelOrderCommand cancelOrderCommand) {
        Objects.requireNonNull(cancelOrderCommand, Constants.Validation.NULL_IDENTIFIER);
        meterRegistry.counter(Constants.Metrics.LIVE_EXCHANGE_CALLS).increment();
        Instant now = Instant.now();
        OrderSnapshot snapshot = resolveOrder(cancelOrderCommand.getClientOrderId(), cancelOrderCommand.getExchangeOrderId());
        if (Objects.isNull(snapshot)) {
            return CancelAck.failed(Objects.requireNonNullElse(cancelOrderCommand.getClientOrderId(), "unknown"),
                Constants.Errors.LIVE_ORDER_NOT_FOUND,
                now);
        }
        OrderSnapshot updated = OrderSnapshot.create(snapshot.getClientOrderId(),
            snapshot.getExchangeOrderId(),
            snapshot.getExchangeInstrumentId(),
            snapshot.getSide(),
            snapshot.getType(),
            snapshot.getTimeInForce(),
            snapshot.getQuantity(),
            snapshot.getPrice(),
            OrderStatus.CANCELED,
            snapshot.getFilledQuantity(),
            snapshot.getAverageFillPrice(),
            snapshot.getCreatedAt(),
            now);
        orders.put(updated.getClientOrderId(), updated);
        meterRegistry.counter(Constants.Metrics.LIVE_ORDER_CANCELED).increment();
        return CancelAck.success(updated.getClientOrderId(), now);
    }

    @Override
    public ModifyAck modifyOrder(ModifyOrderCommand modifyOrderCommand) {
        Objects.requireNonNull(modifyOrderCommand, Constants.Validation.NULL_IDENTIFIER);
        meterRegistry.counter(Constants.Metrics.LIVE_EXCHANGE_CALLS).increment();
        Instant now = Instant.now();
        OrderSnapshot snapshot = orders.get(modifyOrderCommand.getClientOrderId());
        if (Objects.isNull(snapshot)) {
            return ModifyAck.failed(modifyOrderCommand.getClientOrderId(), Constants.Errors.LIVE_ORDER_NOT_FOUND, now);
        }
        OrderSnapshot updated = OrderSnapshot.create(snapshot.getClientOrderId(),
            snapshot.getExchangeOrderId(),
            snapshot.getExchangeInstrumentId(),
            snapshot.getSide(),
            snapshot.getType(),
            snapshot.getTimeInForce(),
            snapshot.getQuantity(),
            Objects.nonNull(modifyOrderCommand.getNewPrice()) ? modifyOrderCommand.getNewPrice() : snapshot.getPrice(),
            snapshot.getStatus(),
            snapshot.getFilledQuantity(),
            snapshot.getAverageFillPrice(),
            snapshot.getCreatedAt(),
            now);
        orders.put(updated.getClientOrderId(), updated);
        return ModifyAck.success(updated.getClientOrderId(), now);
    }

    @Override
    public Optional<OrderSnapshot> getOrder(String clientOrderId, String exchangeOrderId) {
        if (Objects.nonNull(clientOrderId)) {
            return Optional.ofNullable(orders.get(clientOrderId));
        }
        if (Objects.nonNull(exchangeOrderId)) {
            return orders.values().stream()
                .filter(order -> Objects.equals(exchangeOrderId, order.getExchangeOrderId()))
                .findFirst();
        }
        return Optional.empty();
    }

    @Override
    public List<OrderSnapshot> listOpenOrders(Long exchangeInstrumentId) {
        return orders.values().stream()
            .filter(order -> isSameInstrument(exchangeInstrumentId, order))
            .filter(order -> isOpenStatus(order.getStatus()))
            .sorted((left, right) -> left.getCreatedAt().compareTo(right.getCreatedAt()))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<PositionSnapshot> getPositions() {
        return new ArrayList<>(positions.values());
    }

    private OrderStatus resolveInitialStatus(OrderType orderType) {
        if (orderType == OrderType.MARKET) {
            return OrderStatus.FILLED;
        }
        return OrderStatus.SUBMITTED;
    }

    private OrderSnapshot resolveOrder(String clientOrderId, String exchangeOrderId) {
        if (Objects.nonNull(clientOrderId)) {
            return orders.get(clientOrderId);
        }
        if (Objects.nonNull(exchangeOrderId)) {
            return orders.values().stream()
                .filter(order -> Objects.equals(exchangeOrderId, order.getExchangeOrderId()))
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    private boolean isSameInstrument(Long exchangeInstrumentId, OrderSnapshot snapshot) {
        if (Objects.isNull(exchangeInstrumentId)) {
            return true;
        }
        return Objects.equals(exchangeInstrumentId, snapshot.getExchangeInstrumentId());
    }

    private boolean isOpenStatus(OrderStatus status) {
        return status == OrderStatus.NEW
            || status == OrderStatus.SUBMITTED
            || status == OrderStatus.PARTIALLY_FILLED;
    }

    private String buildExchangeOrderId() {
        return String.format("OKX-%d", exchangeSequence.getAndIncrement());
    }

    private BigDecimal deriveAveragePrice(NewOrder newOrder) {
        if (Objects.nonNull(newOrder.getPrice())) {
            return newOrder.getPrice();
        }
        return BigDecimal.ONE;
    }

    private void updatePosition(OrderSnapshot snapshot) {
        BigDecimal signedQuantity = snapshot.getSide() == OrderSide.LONG
            ? snapshot.getQuantity()
            : snapshot.getQuantity().negate();
        positions.compute(snapshot.getExchangeInstrumentId(), (key, current) -> {
            BigDecimal newSize = Objects.isNull(current) ? signedQuantity : current.getSize().add(signedQuantity);
            BigDecimal averagePrice = Objects.isNull(snapshot.getPrice()) ? snapshot.getAverageFillPrice() : snapshot.getPrice();
            if (Objects.nonNull(current) && Objects.nonNull(current.getAveragePrice())) {
                averagePrice = current.getAveragePrice();
            }
            return PositionSnapshot.create(key,
                newSize,
                averagePrice,
                null,
                null,
                BigDecimal.ZERO,
                1,
                true,
                Instant.now().toEpochMilli(),
                Instant.now());
        });
//        meterRegistry.gaugeCollectionSize(Constants.Metrics.LIVE_POSITION_SIZE, positions.entrySet());
    }
}
