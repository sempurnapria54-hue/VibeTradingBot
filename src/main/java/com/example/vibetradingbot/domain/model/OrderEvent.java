package com.example.vibetradingbot.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.example.vibetradingbot.domain.enums.OrderEventType;
import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Журнал события заявки.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
public class OrderEvent {

    private String clientOrderId;
    private String exchangeOrderId;
    private OrderEventType eventType;
    private String payload;
    private Instant timestamp;

    @Builder
    private OrderEvent(String clientOrderId, String exchangeOrderId, OrderEventType eventType, String payload, Instant timestamp) {
        this.clientOrderId = Objects.requireNonNull(clientOrderId, Constants.Validation.NULL_IDENTIFIER);
        this.exchangeOrderId = exchangeOrderId;
        this.eventType = Objects.requireNonNull(eventType, Constants.Validation.NULL_IDENTIFIER);
        this.payload = payload;
        this.timestamp = Objects.requireNonNull(timestamp, Constants.Validation.NULL_IDENTIFIER);
    }

    public static OrderEvent create(String clientOrderId,
        String exchangeOrderId,
        OrderEventType eventType,
        String payload,
        Instant timestamp) {
        return OrderEvent.builder()
            .clientOrderId(clientOrderId)
            .exchangeOrderId(exchangeOrderId)
            .eventType(eventType)
            .payload(payload)
            .timestamp(timestamp)
            .build();
    }
}
