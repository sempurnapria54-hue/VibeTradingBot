package com.example.tradingbot.api.v1.dto;

import java.math.BigDecimal;

public record ExchangeSymbolDto(
    String clientSymbol,
    String contractType,
    String marginMode,
    BigDecimal leverageMax,
    BigDecimal priceStep,
    BigDecimal quantityStep
) {
}
