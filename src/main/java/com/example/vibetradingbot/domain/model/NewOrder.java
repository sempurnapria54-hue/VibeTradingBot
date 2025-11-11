package com.example.vibetradingbot.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import com.example.vibetradingbot.domain.enums.OrderSide;
import com.example.vibetradingbot.domain.enums.OrderTimeInForce;
import com.example.vibetradingbot.domain.enums.OrderType;
import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Доменная команда на создание новой заявки.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
public class NewOrder {

    private String clientOrderId;
    private Long exchangeInstrumentId;
    private OrderSide side;
    private OrderType type;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal stopLoss;
    private BigDecimal takeProfit;
    private OrderTimeInForce timeInForce;
    private Instant createdAt;

    @Builder
    private NewOrder(String clientOrderId,
        Long exchangeInstrumentId,
        OrderSide side,
        OrderType type,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal stopLoss,
        BigDecimal takeProfit,
        OrderTimeInForce timeInForce,
        Instant createdAt) {
        this.clientOrderId = Objects.requireNonNull(clientOrderId, Constants.Validation.NULL_IDENTIFIER);
        this.exchangeInstrumentId = Objects.requireNonNull(exchangeInstrumentId, Constants.Validation.NULL_IDENTIFIER);
        this.side = Objects.requireNonNull(side, Constants.Validation.NULL_IDENTIFIER);
        this.type = Objects.requireNonNull(type, Constants.Validation.NULL_IDENTIFIER);
        this.quantity = Objects.requireNonNull(quantity, Constants.Validation.NULL_IDENTIFIER);
        this.price = price;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
        this.timeInForce = Objects.requireNonNull(timeInForce, Constants.Validation.NULL_IDENTIFIER);
        this.createdAt = Objects.requireNonNull(createdAt, Constants.Validation.NULL_IDENTIFIER);
    }

    public static NewOrder create(String clientOrderId,
        Long exchangeInstrumentId,
        OrderSide side,
        OrderType type,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal stopLoss,
        BigDecimal takeProfit,
        OrderTimeInForce timeInForce,
        Instant createdAt) {
        return NewOrder.builder()
            .clientOrderId(clientOrderId)
            .exchangeInstrumentId(exchangeInstrumentId)
            .side(side)
            .type(type)
            .quantity(quantity)
            .price(price)
            .stopLoss(stopLoss)
            .takeProfit(takeProfit)
            .timeInForce(timeInForce)
            .createdAt(createdAt)
            .build();
    }
}
