package com.example.tradingbot.domain.model;

import java.util.Objects;

public final class Exchange {

    private final Long id;
    private final String name;

    private Exchange(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Exchange create(String name) {
        Objects.requireNonNull(name, "name");
        return new Exchange(null, name);
    }

    public Exchange withId(Long newId) {
        Objects.requireNonNull(newId, "newId");
        return new Exchange(newId, name);
    }

    public static Exchange rehydrate(Long id, String name) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        return new Exchange(id, name);
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }
}
