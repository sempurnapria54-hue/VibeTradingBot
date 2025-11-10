package com.example.tradingbot.domain.value;

import java.util.Objects;

public final class Timeframe {

    private final String canonical;

    private Timeframe(String canonical) {
        this.canonical = canonical;
    }

    public static Timeframe of(String canonical, java.util.Set<String> allowed) {
        Objects.requireNonNull(canonical, "canonical");
        Objects.requireNonNull(allowed, "allowed");
        if (!allowed.contains(canonical)) {
            throw new IllegalArgumentException("Unsupported timeframe: " + canonical);
        }
        return new Timeframe(canonical);
    }

    public String canonical() {
        return canonical;
    }

    @Override
    public String toString() {
        return canonical;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Timeframe timeframe = (Timeframe) o;
        return canonical.equals(timeframe.canonical);
    }

    @Override
    public int hashCode() {
        return canonical.hashCode();
    }
}
