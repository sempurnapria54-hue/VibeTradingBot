package com.example.vibetradingbot.api.v1.dto;

import java.time.Instant;

import com.example.vibetradingbot.domain.enums.OrderEventType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO события ордера.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveEventDto {

    private String clientOrderId;
    private String exchangeOrderId;
    private OrderEventType eventType;
    private String payload;
    private Instant timestamp;
}
