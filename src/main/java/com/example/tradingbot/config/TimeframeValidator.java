package com.example.tradingbot.config;

import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class TimeframeValidator {

    private final TimeframeProperties timeframeProperties;
    private final AppProperties appProperties;

    public TimeframeValidator(TimeframeProperties timeframeProperties, AppProperties appProperties) {
        this.timeframeProperties = timeframeProperties;
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void validate() {
        List<String> canonical = timeframeProperties.getCanonical();
        if (canonical == null || canonical.isEmpty()) {
            throw new IllegalStateException("Canonical timeframes are not configured");
        }
        Map<String, Map<String, String>> exchanges = timeframeProperties.getExchanges();
        if (exchanges == null) {
            throw new IllegalStateException("Timeframe exchanges mapping is not configured");
        }
        if (appProperties.getExchanges() == null) {
            return;
        }
        List<String> requiredExchanges = appProperties.getExchanges().getRequired();
        if (requiredExchanges == null) {
            return;
        }
        for (String exchange : requiredExchanges) {
            Map<String, String> mapping = exchanges.get(exchange);
            if (mapping == null) {
                throw new IllegalStateException("Missing timeframe mapping for exchange " + exchange);
            }
            if (!mapping.keySet().containsAll(canonical)) {
                throw new IllegalStateException("Exchange " + exchange + " does not define all canonical timeframes");
            }
        }
    }
}
