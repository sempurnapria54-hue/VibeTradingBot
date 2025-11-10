package com.example.tradingbot.exchange;

import java.time.Duration;
import java.time.Instant;

public interface ExchangeClock {
    Duration skew();
    Instant nowExchange();
}
