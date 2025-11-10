package com.example.tradingbot.persistence.model;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CandleEntity {
    private Long id;
    private Long historyGroupId;
    private Instant timestampUtc;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private BigDecimal volumeCoin;
    private BigDecimal volumeCurrency;
    private Instant createdAt;
}
