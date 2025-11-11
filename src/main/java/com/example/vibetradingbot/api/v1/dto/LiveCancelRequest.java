package com.example.vibetradingbot.api.v1.dto;

import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос на отмену заявки.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveCancelRequest {

    @Size(min = 1, max = 128)
    private String clientOrderId;

    @Size(min = 1, max = 128)
    private String exchangeOrderId;
}
