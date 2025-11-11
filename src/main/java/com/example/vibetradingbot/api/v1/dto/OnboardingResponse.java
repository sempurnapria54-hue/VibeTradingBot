package com.example.vibetradingbot.api.v1.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;

/**
 * DTO ответа на онбординг.
 */
@Value
@Builder
public class OnboardingResponse {

    UUID exchangeId;
    UUID instrumentId;
    String message;
}
