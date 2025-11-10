package com.example.tradingbot.domain.model.params;

import java.time.Instant;
import java.util.Objects;

public final class RiskParams {

    private final Long id;
    private final String version;
    private final String canonicalJson;
    private final Instant createdAt;
    private final String createdBy;
    private final boolean active;

    private RiskParams(
        Long id,
        String version,
        String canonicalJson,
        Instant createdAt,
        String createdBy,
        boolean active
    ) {
        this.id = id;
        this.version = version;
        this.canonicalJson = canonicalJson;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.active = active;
    }

    public static RiskParams create(
        String version,
        String canonicalJson,
        Instant createdAt,
        String createdBy,
        boolean active
    ) {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(canonicalJson, "canonicalJson");
        Instant created = createdAt != null ? createdAt : Instant.now();
        return new RiskParams(null, version, canonicalJson, created, createdBy, active);
    }

    public RiskParams withId(Long newId) {
        Objects.requireNonNull(newId, "newId");
        return new RiskParams(newId, version, canonicalJson, createdAt, createdBy, active);
    }

    public static RiskParams rehydrate(
        Long id,
        String version,
        String canonicalJson,
        Instant createdAt,
        String createdBy,
        boolean active
    ) {
        Objects.requireNonNull(id, "id");
        return new RiskParams(id, version, canonicalJson, createdAt, createdBy, active);
    }

    public Long id() {
        return id;
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
