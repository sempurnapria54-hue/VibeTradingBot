package com.example.tradingbot.persistence.model;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeInstrumentEntity {
    private Long id;
    private Long exchangeId;
    private Long instrumentId;
    private String clientSymbol;
    private String contractType;
    private String marginMode;
    private BigDecimal leverageMax;
    private Instant createdAt;
}
