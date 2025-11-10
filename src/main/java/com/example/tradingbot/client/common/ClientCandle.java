package com.example.tradingbot.client.common;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClientCandle {
    Instant timestamp;
    BigDecimal open;
    BigDecimal high;
    BigDecimal low;
    BigDecimal close;
    BigDecimal volumeBase;
    BigDecimal volumeQuote;
}
