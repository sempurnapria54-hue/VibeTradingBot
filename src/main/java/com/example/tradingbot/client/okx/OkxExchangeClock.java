package com.example.tradingbot.client.okx;

import com.example.tradingbot.exchange.ExchangeClock;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OkxExchangeClock implements ExchangeClock {

    private final AtomicReference<Duration> skew = new AtomicReference<>(Duration.ZERO);

    @Override
    public Duration skew() {
        return skew.get();
    }

    @Override
    public Instant nowExchange() {
        return Instant.now().plus(skew());
    }

    @Scheduled(fixedDelay = 60000)
    public void recalibrate() {
        skew.set(Duration.ZERO);
    }
}
