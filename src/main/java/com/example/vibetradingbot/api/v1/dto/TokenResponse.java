package com.example.vibetradingbot.api.v1.dto;

import lombok.Builder;
import lombok.Value;

/**
 * DTO ответа с токенами.
 */
@Value
@Builder
public class TokenResponse {

    String accessToken;
    String refreshToken;
}
