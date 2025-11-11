package com.example.vibetradingbot.api.v1.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Value;

/**
 * DTO запроса на получение тестового токена.
 */
@Value
public class TokenRequest {

    @NotBlank
    String username;
}
