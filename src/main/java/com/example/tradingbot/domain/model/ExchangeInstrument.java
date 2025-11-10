package com.example.tradingbot.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class ExchangeInstrument {

    private final Long id;
    private final Exchange exchange;
    private final Instrument instrument;
    private final String clientSymbol;
    private final String contractType;
    private final String marginMode;
    private final BigDecimal leverageMax;

    private ExchangeInstrument(
        Long id,
        Exchange exchange,
        Instrument instrument,
        String clientSymbol,
        String contractType,
        String marginMode,
        BigDecimal leverageMax
    ) {
        this.id = id;
        this.exchange = exchange;
        this.instrument = instrument;
        this.clientSymbol = clientSymbol;
        this.contractType = contractType;
        this.marginMode = marginMode;
        this.leverageMax = leverageMax;
    }

    public static ExchangeInstrument create(
        Exchange exchange,
        Instrument instrument,
        String clientSymbol,
        String contractType,
        String marginMode,
        BigDecimal leverageMax
    ) {
        Objects.requireNonNull(exchange, "exchange");
        Objects.requireNonNull(instrument, "instrument");
        Objects.requireNonNull(clientSymbol, "clientSymbol");
        Objects.requireNonNull(contractType, "contractType");
        Objects.requireNonNull(marginMode, "marginMode");
        if (leverageMax != null && leverageMax.signum() < 0) {
            throw new IllegalArgumentException("leverageMax cannot be negative");
        }
        return new ExchangeInstrument(null, exchange, instrument, clientSymbol, contractType, marginMode, leverageMax);
    }

    public ExchangeInstrument withId(Long newId) {
        Objects.requireNonNull(newId, "newId");
        return new ExchangeInstrument(newId, exchange, instrument, clientSymbol, contractType, marginMode, leverageMax);
    }

    public static ExchangeInstrument rehydrate(
        Long id,
        Exchange exchange,
        Instrument instrument,
        String clientSymbol,
        String contractType,
        String marginMode,
        BigDecimal leverageMax
    ) {
        Objects.requireNonNull(id, "id");
        return new ExchangeInstrument(id, exchange, instrument, clientSymbol, contractType, marginMode, leverageMax);
    }

    public Long id() {
        return id;
    }

    public Exchange exchange() {
        return exchange;
    }

    public Instrument instrument() {
        return instrument;
    }

    public String clientSymbol() {
        return clientSymbol;
    }

    public String contractType() {
        return contractType;
    }

    public String marginMode() {
        return marginMode;
    }

    public BigDecimal leverageMax() {
        return leverageMax;
    }
}
