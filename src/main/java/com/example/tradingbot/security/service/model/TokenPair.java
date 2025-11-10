package com.example.tradingbot.security.service.model;

import lombok.Value;

@Value
public class TokenPair {
    String accessToken;
    String refreshToken;
}
