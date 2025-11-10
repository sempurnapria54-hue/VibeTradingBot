package com.example.tradingbot.domain.value;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

public final class TimestampUtc {

    private final Instant value;

    private TimestampUtc(Instant value) {
        this.value = value;
    }

    public static TimestampUtc of(Instant value) {
        Objects.requireNonNull(value, "value");
        return new TimestampUtc(value);
    }

    public Instant toInstant() {
        return value;
    }

    public ZonedDateTime toZonedDateTime() {
        return value.atZone(ZoneOffset.UTC);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimestampUtc that = (TimestampUtc) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
