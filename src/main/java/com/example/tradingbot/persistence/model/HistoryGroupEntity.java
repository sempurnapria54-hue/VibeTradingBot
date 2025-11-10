package com.example.tradingbot.persistence.model;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryGroupEntity {
    private Long id;
    private Long exchangeInstrumentId;
    private String timeframeCanonical;
    private Instant createdAt;
    private Instant coverageStartUtc;
}
