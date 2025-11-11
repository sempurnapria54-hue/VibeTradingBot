package com.example.vibetradingbot.api.v1.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.vibetradingbot.domain.enums.OrderSide;
import com.example.vibetradingbot.domain.enums.OrderStatus;
import com.example.vibetradingbot.domain.enums.OrderTimeInForce;
import com.example.vibetradingbot.domain.enums.OrderType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO состояния заявки.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveOrderDto {

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
}
