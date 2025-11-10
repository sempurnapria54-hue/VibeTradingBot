package com.example.tradingbot.domain.model.params;

import com.example.tradingbot.domain.model.Exchange;

import java.time.Instant;
import java.util.Objects;

public final class ExchangeParams {

    private final Long id;
    private final Exchange exchange;
    private final String version;
    private final String canonicalJson;
    private final Instant createdAt;
    private final String createdBy;
    private final boolean active;

    private ExchangeParams(
        Long id,
        Exchange exchange,
        String version,
        String canonicalJson,
        Instant createdAt,
        String createdBy,
        boolean active
    ) {
        this.id = id;
        this.exchange = exchange;
        this.version = version;
        this.canonicalJson = canonicalJson;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.active = active;
    }

    public static ExchangeParams create(
        Exchange exchange,
        String version,
        String canonicalJson,
        Instant createdAt,
        String createdBy,
        boolean active
    ) {
        Objects.requireNonNull(exchange, "exchange");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(canonicalJson, "canonicalJson");
        Instant created = createdAt != null ? createdAt : Instant.now();
        return new ExchangeParams(null, exchange, version, canonicalJson, created, createdBy, active);
    }

    public ExchangeParams withId(Long newId) {
        Objects.requireNonNull(newId, "newId");
        return new ExchangeParams(newId, exchange, version, canonicalJson, createdAt, createdBy, active);
    }

    public static ExchangeParams rehydrate(
        Long id,
        Exchange exchange,
        String version,
        String canonicalJson,
        Instant createdAt,
        String createdBy,
        boolean active
    ) {
        Objects.requireNonNull(id, "id");
        return new ExchangeParams(id, exchange, version, canonicalJson, createdAt, createdBy, active);
    }

    public Long id() {
        return id;
    }

    public Exchange exchange() {
        return exchange;
    }

    public String version() {
        return version;
    }

    public String canonicalJson() {
        return canonicalJson;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String createdBy() {
        return createdBy;
    }

    public boolean active() {
        return active;
    }
}
