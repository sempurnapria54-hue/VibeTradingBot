package com.example.tradingbot.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "timeframes")
public class TimeframeProperties {
    private List<String> canonical;
    private Map<String, Map<String, String>> exchanges;
}
