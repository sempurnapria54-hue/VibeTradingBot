package com.example.vibetradingbot.exchange.config;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.example.vibetradingbot.exchange.connector.ExchangeMarketDataConnector;
import com.example.vibetradingbot.exchange.model.ExchangeVendor;
import com.example.vibetradingbot.exception.ServiceException;
import com.example.vibetradingbot.util.Constants;

/**
 * Реестр доступных коннекторов бирж.
 */
@Component
public class ExchangeConnectorRegistry {

    private final Map<ExchangeVendor, ExchangeMarketDataConnector> connectors = new EnumMap<>(ExchangeVendor.class);

    public ExchangeConnectorRegistry(List<ExchangeMarketDataConnector> connectorList) {
        connectorList.forEach(connector -> connectors.put(connector.vendor(), connector));
    }

    public ExchangeMarketDataConnector getConnector(ExchangeVendor vendor) {
        ExchangeMarketDataConnector connector = connectors.get(vendor);
        if (Objects.isNull(connector)) {
            throw new ServiceException(Constants.Errors.RESOURCE_NOT_FOUND, vendor.name());
        }
        return connector;
    }
}
