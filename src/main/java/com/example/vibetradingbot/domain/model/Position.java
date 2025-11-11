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
 * Доменная модель позиции.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
public class Position {

    private Long exchangeInstrumentId;
    private BigDecimal size;
    private BigDecimal averagePrice;
    private BigDecimal stopLoss;
    private BigDecimal takeProfit;
    private BigDecimal unrealizedPnl;
    private int leverage;
    private boolean isolated;
    private long stateVersion;
    private Instant updatedAt;

    @Builder
    private Position(Long exchangeInstrumentId,
        BigDecimal size,
        BigDecimal averagePrice,
        BigDecimal stopLoss,
        BigDecimal takeProfit,
        BigDecimal unrealizedPnl,
        int leverage,
        boolean isolated,
        long stateVersion,
        Instant updatedAt) {
        this.exchangeInstrumentId = Objects.requireNonNull(exchangeInstrumentId, Constants.Validation.NULL_IDENTIFIER);
        this.size = Objects.requireNonNull(size, Constants.Validation.NULL_IDENTIFIER);
        this.averagePrice = averagePrice;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
        this.unrealizedPnl = unrealizedPnl;
        this.leverage = leverage;
        this.isolated = isolated;
        this.stateVersion = stateVersion;
        this.updatedAt = Objects.requireNonNull(updatedAt, Constants.Validation.NULL_IDENTIFIER);
    }

    public static Position create(Long exchangeInstrumentId,
        BigDecimal size,
        BigDecimal averagePrice,
        BigDecimal stopLoss,
        BigDecimal takeProfit,
        BigDecimal unrealizedPnl,
        int leverage,
        boolean isolated,
        long stateVersion,
        Instant updatedAt) {
        return Position.builder()
            .exchangeInstrumentId(exchangeInstrumentId)
            .size(size)
            .averagePrice(averagePrice)
            .stopLoss(stopLoss)
            .takeProfit(takeProfit)
            .unrealizedPnl(unrealizedPnl)
            .leverage(leverage)
            .isolated(isolated)
            .stateVersion(stateVersion)
            .updatedAt(updatedAt)
            .build();
    }
}
