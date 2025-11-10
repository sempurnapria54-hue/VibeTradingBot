package com.example.tradingbot.api.v1.dto;

import java.util.List;

public record SymbolBookResponse(
    List<ExchangeSymbolDto> symbols
) {
}
