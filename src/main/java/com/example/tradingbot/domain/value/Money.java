package com.example.tradingbot.domain.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money {

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    public static Money of(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        if (amount.scale() > 30) {
            throw new IllegalArgumentException("Money scale must be <= 30");
        }
        return new Money(amount.stripTrailingZeros());
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "other");
        return new Money(amount.add(other.amount));
    }

    public Money subtract(Money other) {
        Objects.requireNonNull(other, "other");
        return new Money(amount.subtract(other.amount));
    }

    public Money multiply(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "multiplier");
        BigDecimal value = amount.multiply(multiplier);
        return new Money(value);
    }

    public Money scale(RoundingMode roundingMode, int scale) {
        Objects.requireNonNull(roundingMode, "roundingMode");
        BigDecimal value = amount.setScale(scale, roundingMode);
        return new Money(value);
    }

    public BigDecimal toBigDecimal() {
        return amount;
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
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.stripTrailingZeros().hashCode();
    }
}
