package com.example.tradingbot.rest;

import com.example.tradingbot.api.v1.dto.ExchangeSymbolDto;
import com.example.tradingbot.api.v1.dto.ExchangeTimeResponse;
import com.example.tradingbot.api.v1.dto.ResolveInstrumentRequest;
import com.example.tradingbot.api.v1.dto.SymbolBookResponse;
import com.example.tradingbot.client.common.ExchangeSymbol;
import com.example.tradingbot.client.common.SymbolBook;
import com.example.tradingbot.domain.service.ExchangeRegistry;
import com.example.tradingbot.exchange.ExchangeConnector;
import com.example.tradingbot.exchange.TimeframeTranslator;
import com.example.tradingbot.mapping.ApiToDomainMapper;
import com.example.tradingbot.mapping.DomainToApiMapper;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchanges")
public class ExchangeController {

    private final ExchangeRegistry registry;
    private final TimeframeTranslator timeframeTranslator;
    private final TimeframeProperties timeframeProperties;
    private final ApiToDomainMapper apiToDomainMapper;
    private final DomainToApiMapper domainToApiMapper;

    public ExchangeController(
        ExchangeRegistry registry,
        TimeframeTranslator timeframeTranslator,
        TimeframeProperties timeframeProperties,
        ApiToDomainMapper apiToDomainMapper,
        DomainToApiMapper domainToApiMapper
    ) {
        this.registry = registry;
        this.timeframeTranslator = timeframeTranslator;
        this.timeframeProperties = timeframeProperties;
        this.apiToDomainMapper = apiToDomainMapper;
        this.domainToApiMapper = domainToApiMapper;
    }

    @GetMapping("/{id}/ping")
    public ResponseEntity<Void> ping(@PathVariable String id) {
        registry.get(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/time")
    public ResponseEntity<ExchangeTimeResponse> time(@PathVariable String id) {
        ExchangeConnector connector = registry.get(id);
        return ResponseEntity.ok(new ExchangeTimeResponse(connector.clock().nowExchange(), connector.clock().skew()));
    }

    @GetMapping("/{id}/symbols")
    public ResponseEntity<SymbolBookResponse> symbols(@PathVariable String id) {
        ExchangeConnector connector = registry.get(id);
        SymbolBook book = connector.listSymbols();
        List<ExchangeSymbolDto> symbols = (book.getSymbols() != null ? book.getSymbols() : List.<ExchangeSymbol>of()).stream()
            .map(domainToApiMapper::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(new SymbolBookResponse(symbols));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ExchangeSymbolDto> resolve(@PathVariable String id, @Valid @RequestBody ResolveInstrumentRequest request) {
        ExchangeConnector connector = registry.get(id);
        ExchangeSymbol symbol = connector.resolve(apiToDomainMapper.toInstrument(request));
        return ResponseEntity.ok(domainToApiMapper.toDto(symbol));
    }

    @GetMapping("/{id}/timeframes/validate")
    public ResponseEntity<Void> validateTimeframes(@PathVariable String id) {
        for (String canonical : timeframeProperties.getCanonical()) {
            String exchangeCode = timeframeTranslator.toExchangeCode(id, canonical);
            timeframeTranslator.fromExchangeCode(id, exchangeCode);
        }
        return ResponseEntity.ok().build();
    }

}
