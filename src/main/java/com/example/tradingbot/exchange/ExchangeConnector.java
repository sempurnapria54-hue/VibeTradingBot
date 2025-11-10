package com.example.tradingbot.exchange;

import com.example.tradingbot.client.common.ClientCandle;
import com.example.tradingbot.client.common.ExchangeSymbol;
import com.example.tradingbot.client.common.SymbolBook;
import com.example.tradingbot.domain.model.Instrument;

import java.time.Instant;
import java.util.List;

public interface ExchangeConnector {
    String id();
    ExchangeClock clock();
    SymbolBook listSymbols();
    ExchangeSymbol resolve(Instrument instrument);
    List<ClientCandle> loadClosedCandles(ExchangeSymbol symbol, String exchangeTimeframeCode, Instant beforeExclusive, int limit);
}
