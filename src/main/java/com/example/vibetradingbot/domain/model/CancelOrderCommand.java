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
 * Команда отмены заявки.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
public class CancelOrderCommand {

    private String clientOrderId;
    private String exchangeOrderId;
    private Instant createdAt;

    @Builder
    private CancelOrderCommand(String clientOrderId, String exchangeOrderId, Instant createdAt) {
        if (Objects.isNull(clientOrderId) && Objects.isNull(exchangeOrderId)) {
            throw new IllegalArgumentException(Constants.Validation.NULL_IDENTIFIER);
        }
        this.clientOrderId = clientOrderId;
        this.exchangeOrderId = exchangeOrderId;
        this.createdAt = Objects.requireNonNull(createdAt, Constants.Validation.NULL_IDENTIFIER);
    }

    public static CancelOrderCommand byClientOrderId(String clientOrderId, Instant createdAt) {
        return CancelOrderCommand.builder()
            .clientOrderId(Objects.requireNonNull(clientOrderId, Constants.Validation.NULL_IDENTIFIER))
            .createdAt(createdAt)
            .build();
    }

    public static CancelOrderCommand byExchangeOrderId(String exchangeOrderId, Instant createdAt) {
        return CancelOrderCommand.builder()
            .exchangeOrderId(Objects.requireNonNull(exchangeOrderId, Constants.Validation.NULL_IDENTIFIER))
            .createdAt(createdAt)
            .build();
    }
}
