package com.example.vibetradingbot.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import com.example.vibetradingbot.domain.enums.OrderSide;
import com.example.vibetradingbot.domain.enums.OrderStatus;
import com.example.vibetradingbot.domain.enums.OrderTimeInForce;
import com.example.vibetradingbot.domain.enums.OrderType;
import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Доменная модель заявки.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
public class Order {

    private String clientOrderId;
    private String exchangeOrderId;
    private Long exchangeInstrumentId;
    private OrderSide side;
    private OrderType type;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal stopLoss;
    private BigDecimal takeProfit;
    private OrderTimeInForce timeInForce;
    private OrderStatus status;
    private String reason;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder
    private Order(String clientOrderId,
        String exchangeOrderId,
        Long exchangeInstrumentId,
        OrderSide side,
        OrderType type,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal stopLoss,
        BigDecimal takeProfit,
        OrderTimeInForce timeInForce,
        OrderStatus status,
        String reason,
        Instant createdAt,
        Instant updatedAt) {
        this.clientOrderId = Objects.requireNonNull(clientOrderId, Constants.Validation.NULL_IDENTIFIER);
        this.exchangeOrderId = exchangeOrderId;
        this.exchangeInstrumentId = Objects.requireNonNull(exchangeInstrumentId, Constants.Validation.NULL_IDENTIFIER);
        this.side = Objects.requireNonNull(side, Constants.Validation.NULL_IDENTIFIER);
        this.type = Objects.requireNonNull(type, Constants.Validation.NULL_IDENTIFIER);
        this.quantity = Objects.requireNonNull(quantity, Constants.Validation.NULL_IDENTIFIER);
        this.price = price;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
        this.timeInForce = Objects.requireNonNull(timeInForce, Constants.Validation.NULL_IDENTIFIER);
        this.status = Objects.requireNonNull(status, Constants.Validation.NULL_IDENTIFIER);
        this.reason = reason;
        this.createdAt = Objects.requireNonNull(createdAt, Constants.Validation.NULL_IDENTIFIER);
        this.updatedAt = Objects.requireNonNull(updatedAt, Constants.Validation.NULL_IDENTIFIER);
    }

    public static Order create(String clientOrderId,
        String exchangeOrderId,
        Long exchangeInstrumentId,
        OrderSide side,
        OrderType type,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal stopLoss,
        BigDecimal takeProfit,
        OrderTimeInForce timeInForce,
        OrderStatus status,
        String reason,
        Instant createdAt,
        Instant updatedAt) {
        return Order.builder()
            .clientOrderId(clientOrderId)
            .exchangeOrderId(exchangeOrderId)
            .exchangeInstrumentId(exchangeInstrumentId)
            .side(side)
            .type(type)
            .quantity(quantity)
            .price(price)
            .stopLoss(stopLoss)
            .takeProfit(takeProfit)
            .timeInForce(timeInForce)
            .status(status)
            .reason(reason)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }
}
