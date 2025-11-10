package com.example.tradingbot.persistence.model;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstrumentEntity {
    private Long id;
    private String name;
    private String base;
    private String quote;
    private BigDecimal priceStep;
    private BigDecimal qtyStep;
    private BigDecimal minNotional;
    private Boolean isPerpetual;
    private Instant createdAt;
}
