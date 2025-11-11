package com.example.vibetradingbot.api.v1.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO события сигнала для ответа API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalEventDto {

    private Long id;
    private UUID exchangeInstrumentId;
    private String timeframe;
    private Instant timestamp;
    private String signalType;
    private String direction;
    private BigDecimal score;
    private Map<String, Object> reason;
    private Long paramsRef;
    private Integer version;
    private Instant createdAt;
}
