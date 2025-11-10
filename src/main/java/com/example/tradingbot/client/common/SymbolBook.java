package com.example.tradingbot.client.common;

import java.util.List;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class SymbolBook {
    @Singular
    List<ExchangeSymbol> symbols;
}
