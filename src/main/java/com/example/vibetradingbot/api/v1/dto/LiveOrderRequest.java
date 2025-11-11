package com.example.vibetradingbot.api.v1.dto;

import java.math.BigDecimal;

import com.example.vibetradingbot.domain.enums.OrderSide;
import com.example.vibetradingbot.domain.enums.OrderTimeInForce;
import com.example.vibetradingbot.domain.enums.OrderType;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос на размещение заявки в live-режиме.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveOrderRequest {

    @NotNull
    private Long exchangeInstrumentId;

    @NotNull
    private OrderSide side;

    private OrderType type;

    @NotNull
    private BigDecimal quantity;

    private BigDecimal price;

    private BigDecimal stopLoss;

    private BigDecimal takeProfit;

    private OrderTimeInForce timeInForce;
}
