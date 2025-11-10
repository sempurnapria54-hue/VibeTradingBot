package com.example.tradingbot.api.v1.dto;

import java.time.Duration;
import java.time.Instant;

public record ExchangeTimeResponse(
    Instant serverTime,
    Duration skew
) {
}
