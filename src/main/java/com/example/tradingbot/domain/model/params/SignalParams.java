package com.example.tradingbot.domain.model.params;

import java.time.Instant;
import java.util.Objects;

public final class SignalParams {

    private final Long id;
    private final String type;
    private final String timeframeCanonical;
    private final String version;
    private final String canonicalJson;
    private final Instant createdAt;
    private final String createdBy;
    private final boolean active;

    private SignalParams(
        Long id,
        String type,
        String timeframeCanonical,
        String version,
        String canonicalJson,
        Instant createdAt,
        String createdBy,
        boolean active
    ) {
        this.id = id;
        this.type = type;
        this.timeframeCanonical = timeframeCanonical;
        this.version = version;
        this.canonicalJson = canonicalJson;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.active = active;
    }

    public static SignalParams create(
        String type,
        String timeframeCanonical,
        String version,
        String canonicalJson,
        Instant createdAt,
        String createdBy,
        boolean active
    ) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(canonicalJson, "canonicalJson");
        Instant created = createdAt != null ? createdAt : Instant.now();
        return new SignalParams(null, type, timeframeCanonical, version, canonicalJson, created, createdBy, active);
    }

    public SignalParams withId(Long newId) {
        Objects.requireNonNull(newId, "newId");
        return new SignalParams(newId, type, timeframeCanonical, version, canonicalJson, createdAt, createdBy, active);
    }

    public static SignalParams rehydrate(
        Long id,
        String type,
        String timeframeCanonical,
        String version,
        String canonicalJson,
        Instant createdAt,
        String createdBy,
        boolean active
    ) {
        Objects.requireNonNull(id, "id");
        return new SignalParams(id, type, timeframeCanonical, version, canonicalJson, createdAt, createdBy, active);
    }

    public Long id() {
        return id;
    }

    public String type() {
        return type;
    }

    public String timeframeCanonical() {
        return timeframeCanonical;
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
