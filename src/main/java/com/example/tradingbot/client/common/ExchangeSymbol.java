package com.example.tradingbot.client.common;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ExchangeSymbol {
    String clientSymbol;
    String contractType;
    String marginMode;
    BigDecimal leverageMax;
    BigDecimal priceStep;
    BigDecimal quantityStep;
}
