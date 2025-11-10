package com.example.tradingbot.api.v1.dto;

public record TokenResponse(
    String accessToken,
    String refreshToken,
    long expiresIn,
    String tokenType
) {
}
