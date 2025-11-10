package com.example.tradingbot.persistence.model;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeInstrumentCoverageEntity {
    private Long exchangeInstrumentId;
    private Instant coverageStartUtc;
    private Instant coverageEndUtc;
    private Instant updatedAt;
}
