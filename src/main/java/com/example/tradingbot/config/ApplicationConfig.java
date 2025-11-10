package com.example.tradingbot.config;

import com.example.tradingbot.security.model.JwtProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({JwtProperties.class, OkxProperties.class, TimeframeProperties.class, AppProperties.class})
public class ApplicationConfig {

    @Bean
    public String activeProfiles(Environment environment) {
        return String.join(",",
            environment.getActiveProfiles()
        );
    }
}
