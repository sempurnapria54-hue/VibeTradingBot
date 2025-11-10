package com.example.tradingbot.domain.value;

import java.math.BigDecimal;
import java.util.Objects;

public final class Quantity {

    private final BigDecimal amount;

    private Quantity(BigDecimal amount) {
        this.amount = amount;
    }

    public static Quantity of(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }
        return new Quantity(amount.stripTrailingZeros());
    }

    public BigDecimal toBigDecimal() {
        return amount;
    }

    public Quantity add(Quantity other) {
        Objects.requireNonNull(other, "other");
        return new Quantity(amount.add(other.amount));
    }

    public Quantity subtract(Quantity other) {
        Objects.requireNonNull(other, "other");
        BigDecimal result = amount.subtract(other.amount);
        if (result.signum() < 0) {
            throw new IllegalArgumentException("Quantity cannot become negative");
        }
        return new Quantity(result);
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Quantity quantity = (Quantity) o;
        return amount.compareTo(quantity.amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.stripTrailingZeros().hashCode();
    }
}
