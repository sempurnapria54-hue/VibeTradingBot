package com.example.tradingbot.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Exchanges exchanges = new Exchanges();

    @Getter
    @Setter
    public static class Exchanges {
        private List<String> required;
    }
}
