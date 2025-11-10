package com.example.tradingbot.exchange;

public interface TimeframeTranslator {
    String toExchangeCode(String exchangeId, String canonicalTimeframe);
    String fromExchangeCode(String exchangeId, String exchangeCode);
}
