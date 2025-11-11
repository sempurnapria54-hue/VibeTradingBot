package com.example.vibetradingbot.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.example.vibetradingbot.domain.enums.OrderStatus;
import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Подтверждение создания заявки.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
public class OrderAck {

    private String clientOrderId;
    private String exchangeOrderId;
    private OrderStatus status;
    private boolean accepted;
    private String message;
    private Instant receivedAt;

    @Builder
    private OrderAck(String clientOrderId,
        String exchangeOrderId,
        OrderStatus status,
        boolean accepted,
        String message,
        Instant receivedAt) {
        this.clientOrderId = Objects.requireNonNull(clientOrderId, Constants.Validation.NULL_IDENTIFIER);
        this.exchangeOrderId = exchangeOrderId;
        this.status = Objects.requireNonNull(status, Constants.Validation.NULL_IDENTIFIER);
        this.accepted = accepted;
        this.message = message;
        this.receivedAt = Objects.requireNonNull(receivedAt, Constants.Validation.NULL_IDENTIFIER);
    }

    public static OrderAck accepted(String clientOrderId, String exchangeOrderId, OrderStatus status, Instant receivedAt) {
        return OrderAck.builder()
            .clientOrderId(clientOrderId)
            .exchangeOrderId(exchangeOrderId)
            .status(status)
            .accepted(true)
            .receivedAt(receivedAt)
            .build();
    }

    public static OrderAck rejected(String clientOrderId, String message, Instant receivedAt) {
        return OrderAck.builder()
            .clientOrderId(clientOrderId)
            .status(OrderStatus.REJECTED)
            .accepted(false)
            .message(message)
            .receivedAt(receivedAt)
            .build();
    }
}
