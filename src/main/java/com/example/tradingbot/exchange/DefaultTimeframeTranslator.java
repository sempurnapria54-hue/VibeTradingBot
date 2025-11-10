package com.example.tradingbot.exchange;

import com.example.tradingbot.config.TimeframeProperties;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class DefaultTimeframeTranslator implements TimeframeTranslator {

    private final Map<String, Map<String, String>> exchanges;

    public DefaultTimeframeTranslator(TimeframeProperties properties) {
        this.exchanges = properties.getExchanges();
    }

    @Override
    public String toExchangeCode(String exchangeId, String canonicalTimeframe) {
        Map<String, String> mapping = lookup(exchangeId);
        String value = mapping.get(canonicalTimeframe);
        if (value == null) {
            throw new IllegalArgumentException("No timeframe mapping for " + canonicalTimeframe + " on " + exchangeId);
        }
        return value;
    }

    @Override
    public String fromExchangeCode(String exchangeId, String exchangeCode) {
        Map<String, String> mapping = lookup(exchangeId);
        return mapping.entrySet().stream()
            .filter(entry -> entry.getValue().equalsIgnoreCase(exchangeCode))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown exchange code " + exchangeCode + " for " + exchangeId));
    }

    private Map<String, String> lookup(String exchangeId) {
        Map<String, String> mapping = exchanges.get(exchangeId);
        if (mapping == null) {
            throw new IllegalArgumentException("Exchange timeframe mapping missing for " + exchangeId);
        }
        return mapping;
    }
}
