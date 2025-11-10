package com.example.tradingbot.client.okx;

import com.example.tradingbot.client.common.ClientCandle;
import com.example.tradingbot.client.common.ExchangeSymbol;
import com.example.tradingbot.client.common.SymbolBook;
import com.example.tradingbot.client.okx.model.OkxSymbolResponse;
import com.example.tradingbot.config.OkxProperties;
import com.example.tradingbot.domain.model.Instrument;
import com.example.tradingbot.exchange.ExchangeClock;
import com.example.tradingbot.exchange.ExchangeConnector;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OkxExchangeConnector implements ExchangeConnector {

    private static final Logger log = LoggerFactory.getLogger(OkxExchangeConnector.class);

    private final OkxProperties properties;
    private final WebClient webClient;
    private final OkxExchangeClock clock;

    public OkxExchangeConnector(
        OkxProperties properties,
        OkxExchangeClock clock,
        WebClient.Builder builder
    ) {
        this.properties = properties;
        this.clock = clock;
        this.webClient = builder.baseUrl(properties.getBaseUrl()).build();
    }

    @Override
    public String id() {
        return "okx";
    }

    @Override
    public ExchangeClock clock() {
        return clock;
    }

    @Override
    public SymbolBook listSymbols() {
        try {
            OkxSymbolResponse response = webClient.get()
                .uri("/api/v5/public/instruments?instType=SWAP")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(OkxSymbolResponse.class)
                .block();
            if (response == null || response.getData() == null) {
                return SymbolBook.builder().build();
            }
            SymbolBook.SymbolBookBuilder builder = SymbolBook.builder();
            for (OkxSymbolResponse.OkxSymbol symbol : response.getData()) {
                builder.symbol(ExchangeSymbol.builder()
                    .clientSymbol(symbol.getInstId())
                    .contractType(symbol.getInstType())
                    .marginMode("ISOLATED")
                    .leverageMax(parseDecimal(symbol.getLever()))
                    .priceStep(parseDecimal(symbol.getTickSz()))
                    .quantityStep(parseDecimal(symbol.getLotSz()))
                    .build());
            }
            return builder.build();
        } catch (Exception ex) {
            log.warn("Failed to load OKX symbols", ex);
            return SymbolBook.builder().build();
        }
    }

    @Override
    public ExchangeSymbol resolve(Instrument instrument) {
        return ExchangeSymbol.builder()
            .clientSymbol(instrument.name())
            .contractType("SWAP")
            .marginMode("ISOLATED")
            .priceStep(instrument.priceStep())
            .quantityStep(instrument.qtyStep())
            .leverageMax(BigDecimal.TEN)
            .build();
    }

    @Override
    public List<ClientCandle> loadClosedCandles(ExchangeSymbol symbol, String exchangeTimeframeCode, Instant beforeExclusive, int limit) {
        log.info("loadClosedCandles symbol={} timeframe={} before={} limit={}", symbol.getClientSymbol(), exchangeTimeframeCode, beforeExclusive, limit);
        return Collections.emptyList();
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value);
    }
}
