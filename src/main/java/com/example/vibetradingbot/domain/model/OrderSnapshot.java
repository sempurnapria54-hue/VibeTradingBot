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
 * Снимок состояния заявки.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
public class OrderSnapshot {

    private String clientOrderId;
    private String exchangeOrderId;
    private Long exchangeInstrumentId;
    private OrderSide side;
    private OrderType type;
    private OrderTimeInForce timeInForce;
    private BigDecimal quantity;
    private BigDecimal price;
    private OrderStatus status;
    private BigDecimal filledQuantity;
    private BigDecimal averageFillPrice;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder
    private OrderSnapshot(String clientOrderId,
        String exchangeOrderId,
        Long exchangeInstrumentId,
        OrderSide side,
        OrderType type,
        OrderTimeInForce timeInForce,
        BigDecimal quantity,
        BigDecimal price,
        OrderStatus status,
        BigDecimal filledQuantity,
        BigDecimal averageFillPrice,
        Instant createdAt,
        Instant updatedAt) {
        this.clientOrderId = Objects.requireNonNull(clientOrderId, Constants.Validation.NULL_IDENTIFIER);
        this.exchangeOrderId = exchangeOrderId;
        this.exchangeInstrumentId = Objects.requireNonNull(exchangeInstrumentId, Constants.Validation.NULL_IDENTIFIER);
        this.side = Objects.requireNonNull(side, Constants.Validation.NULL_IDENTIFIER);
        this.type = Objects.requireNonNull(type, Constants.Validation.NULL_IDENTIFIER);
        this.timeInForce = Objects.requireNonNull(timeInForce, Constants.Validation.NULL_IDENTIFIER);
        this.quantity = Objects.requireNonNull(quantity, Constants.Validation.NULL_IDENTIFIER);
        this.price = price;
        this.status = Objects.requireNonNull(status, Constants.Validation.NULL_IDENTIFIER);
        this.filledQuantity = Objects.requireNonNull(filledQuantity, Constants.Validation.NULL_IDENTIFIER);
        this.averageFillPrice = averageFillPrice;
        this.createdAt = Objects.requireNonNull(createdAt, Constants.Validation.NULL_IDENTIFIER);
        this.updatedAt = Objects.requireNonNull(updatedAt, Constants.Validation.NULL_IDENTIFIER);
    }

    public static OrderSnapshot create(String clientOrderId,
        String exchangeOrderId,
        Long exchangeInstrumentId,
        OrderSide side,
        OrderType type,
        OrderTimeInForce timeInForce,
        BigDecimal quantity,
        BigDecimal price,
        OrderStatus status,
        BigDecimal filledQuantity,
        BigDecimal averageFillPrice,
        Instant createdAt,
        Instant updatedAt) {
        return OrderSnapshot.builder()
            .clientOrderId(clientOrderId)
            .exchangeOrderId(exchangeOrderId)
            .exchangeInstrumentId(exchangeInstrumentId)
            .side(side)
            .type(type)
            .timeInForce(timeInForce)
            .quantity(quantity)
            .price(price)
            .status(status)
            .filledQuantity(filledQuantity)
            .averageFillPrice(averageFillPrice)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }
}
