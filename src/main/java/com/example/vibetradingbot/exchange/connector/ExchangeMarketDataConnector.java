package com.example.vibetradingbot.exchange.connector;

import java.time.Instant;
import java.util.List;

import com.example.vibetradingbot.client.model.ClientCandle;
import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.model.ExchangeInstrument;
import com.example.vibetradingbot.exchange.model.ExchangeVendor;

/**
 * Контракт коннектора загрузки рыночных данных.
 */
public interface ExchangeMarketDataConnector {

    ExchangeVendor vendor();

    List<ClientCandle> loadCandles(ExchangeInstrument exchangeInstrument, CanonicalTimeframe timeframe, Instant from,
            Instant to);
}
