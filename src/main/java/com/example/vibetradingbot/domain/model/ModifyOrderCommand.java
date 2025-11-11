package com.example.vibetradingbot.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Команда модификации заявки.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
public class ModifyOrderCommand {

    private String clientOrderId;
    private BigDecimal newPrice;
    private BigDecimal newStopLoss;
    private BigDecimal newTakeProfit;
    private Instant createdAt;

    @Builder
    private ModifyOrderCommand(String clientOrderId,
        BigDecimal newPrice,
        BigDecimal newStopLoss,
        BigDecimal newTakeProfit,
        Instant createdAt) {
        this.clientOrderId = Objects.requireNonNull(clientOrderId, Constants.Validation.NULL_IDENTIFIER);
        this.newPrice = newPrice;
        this.newStopLoss = newStopLoss;
        this.newTakeProfit = newTakeProfit;
        this.createdAt = Objects.requireNonNull(createdAt, Constants.Validation.NULL_IDENTIFIER);
    }

    public static ModifyOrderCommand create(String clientOrderId,
        BigDecimal newPrice,
        BigDecimal newStopLoss,
        BigDecimal newTakeProfit,
        Instant createdAt) {
        if (Objects.isNull(newPrice) && Objects.isNull(newStopLoss) && Objects.isNull(newTakeProfit)) {
            throw new IllegalArgumentException(Constants.Validation.NULL_IDENTIFIER);
        }
        return ModifyOrderCommand.builder()
            .clientOrderId(clientOrderId)
            .newPrice(newPrice)
            .newStopLoss(newStopLoss)
            .newTakeProfit(newTakeProfit)
            .createdAt(createdAt)
            .build();
    }
}
