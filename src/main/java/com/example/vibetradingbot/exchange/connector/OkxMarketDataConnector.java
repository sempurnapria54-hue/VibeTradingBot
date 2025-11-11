package com.example.vibetradingbot.exchange.connector;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.vibetradingbot.client.model.ClientCandle;
import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.model.ExchangeInstrument;
import com.example.vibetradingbot.exchange.model.ExchangeVendor;

/**
 * Заглушка коннектора OKX.
 */
@Component
public class OkxMarketDataConnector implements ExchangeMarketDataConnector {

    @Override
    public ExchangeVendor vendor() {
        return ExchangeVendor.OKX;
    }

    @Override
    public List<ClientCandle> loadCandles(ExchangeInstrument exchangeInstrument, CanonicalTimeframe timeframe, Instant from,
            Instant to) {
        return Collections.emptyList();
    }
}
