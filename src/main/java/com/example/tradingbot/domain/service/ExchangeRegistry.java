package com.example.tradingbot.domain.service;

import com.example.tradingbot.exchange.ExchangeConnector;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class ExchangeRegistry {

    private final Map<String, ExchangeConnector> connectors = new ConcurrentHashMap<>();

    public ExchangeRegistry(List<ExchangeConnector> connectors) {
        connectors.forEach(connector -> this.connectors.put(connector.id(), connector));
    }

    public ExchangeConnector get(String id) {
        ExchangeConnector connector = connectors.get(id);
        if (connector == null) {
            throw new IllegalArgumentException("Unknown exchange: " + id);
        }
        return connector;
    }
}
