package com.example.vibetradingbot.api.v1.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;

/**
 * DTO ответа проверки здоровья сервиса.
 */
@Value
@Builder
public class HealthResponse {

    String status;
    String message;
    Instant timestamp;
}
