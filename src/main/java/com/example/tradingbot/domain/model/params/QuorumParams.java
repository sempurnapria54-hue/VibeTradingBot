package com.example.tradingbot.domain.model.params;

import java.time.Instant;
import java.util.Objects;

public final class QuorumParams {

    private final Long id;
    private final String version;
    private final String canonicalJson;
    private final Instant createdAt;
    private final String createdBy;
    private final boolean active;

    private QuorumParams(
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

    public static QuorumParams create(
        String version,
        String canonicalJson,
        Instant createdAt,
        String createdBy,
        boolean active
    ) {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(canonicalJson, "canonicalJson");
        Instant created = createdAt != null ? createdAt : Instant.now();
        return new QuorumParams(null, version, canonicalJson, created, createdBy, active);
    }

    public QuorumParams withId(Long newId) {
        Objects.requireNonNull(newId, "newId");
        return new QuorumParams(newId, version, canonicalJson, createdAt, createdBy, active);
    }

    public static QuorumParams rehydrate(
        Long id,
        String version,
        String canonicalJson,
        Instant createdAt,
        String createdBy,
        boolean active
    ) {
        Objects.requireNonNull(id, "id");
        return new QuorumParams(id, version, canonicalJson, createdAt, createdBy, active);
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
