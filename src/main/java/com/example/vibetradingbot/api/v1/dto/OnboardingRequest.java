package com.example.vibetradingbot.api.v1.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import lombok.Value;

/**
 * DTO запроса на онбординг.
 */
@Value
public class OnboardingRequest {

    @NotNull
    UUID exchangeId;
    @NotNull
    UUID instrumentId;
    @NotNull
    String timeframe;
}
