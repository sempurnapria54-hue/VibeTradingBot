package com.example.vibetradingbot.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Подтверждение отмены заявки.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
public class CancelAck {

    private String clientOrderId;
    private boolean accepted;
    private String message;
    private Instant receivedAt;

    @Builder
    private CancelAck(String clientOrderId, boolean accepted, String message, Instant receivedAt) {
        this.clientOrderId = Objects.requireNonNull(clientOrderId, Constants.Validation.NULL_IDENTIFIER);
        this.accepted = accepted;
        this.message = message;
        this.receivedAt = Objects.requireNonNull(receivedAt, Constants.Validation.NULL_IDENTIFIER);
    }

    public static CancelAck success(String clientOrderId, Instant receivedAt) {
        return CancelAck.builder()
            .clientOrderId(clientOrderId)
            .accepted(true)
            .receivedAt(receivedAt)
            .build();
    }

    public static CancelAck failed(String clientOrderId, String message, Instant receivedAt) {
        return CancelAck.builder()
            .clientOrderId(clientOrderId)
            .accepted(false)
            .message(message)
            .receivedAt(receivedAt)
            .build();
    }
}
