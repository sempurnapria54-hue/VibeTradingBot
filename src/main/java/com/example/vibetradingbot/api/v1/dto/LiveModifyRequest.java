package com.example.vibetradingbot.api.v1.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос на модификацию заявки.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveModifyRequest {

    @NotBlank
    private String clientOrderId;

    private BigDecimal newPrice;

    private BigDecimal newStopLoss;

    private BigDecimal newTakeProfit;
}
