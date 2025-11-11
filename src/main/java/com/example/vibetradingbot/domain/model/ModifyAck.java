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
 * Подтверждение модификации заявки.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
public class ModifyAck {

    private String clientOrderId;
    private boolean accepted;
    private String message;
    private Instant receivedAt;

    @Builder
    private ModifyAck(String clientOrderId, boolean accepted, String message, Instant receivedAt) {
        this.clientOrderId = Objects.requireNonNull(clientOrderId, Constants.Validation.NULL_IDENTIFIER);
        this.accepted = accepted;
        this.message = message;
        this.receivedAt = Objects.requireNonNull(receivedAt, Constants.Validation.NULL_IDENTIFIER);
    }

    public static ModifyAck success(String clientOrderId, Instant receivedAt) {
        return ModifyAck.builder()
            .clientOrderId(clientOrderId)
            .accepted(true)
            .receivedAt(receivedAt)
            .build();
    }

    public static ModifyAck failed(String clientOrderId, String message, Instant receivedAt) {
        return ModifyAck.builder()
            .clientOrderId(clientOrderId)
            .accepted(false)
            .message(message)
            .receivedAt(receivedAt)
            .build();
    }
}
