package com.example.tradingbot.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Instrument {

    private final Long id;
    private final String name;
    private final String base;
    private final String quote;
    private final BigDecimal priceStep;
    private final BigDecimal qtyStep;
    private final BigDecimal minNotional;
    private final boolean perpetual;

    private Instrument(
        Long id,
        String name,
        String base,
        String quote,
        BigDecimal priceStep,
        BigDecimal qtyStep,
        BigDecimal minNotional,
        boolean perpetual
    ) {
        this.id = id;
        this.name = name;
        this.base = base;
        this.quote = quote;
        this.priceStep = priceStep;
        this.qtyStep = qtyStep;
        this.minNotional = minNotional;
        this.perpetual = perpetual;
    }

    public static Instrument create(
        String name,
        String base,
        String quote,
        BigDecimal priceStep,
        BigDecimal qtyStep,
        BigDecimal minNotional,
        boolean perpetual
    ) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(quote, "quote");
        Objects.requireNonNull(priceStep, "priceStep");
        Objects.requireNonNull(qtyStep, "qtyStep");
        if (priceStep.signum() <= 0) {
            throw new IllegalArgumentException("priceStep must be positive");
        }
        if (qtyStep.signum() <= 0) {
            throw new IllegalArgumentException("qtyStep must be positive");
        }
        if (minNotional != null && minNotional.signum() < 0) {
            throw new IllegalArgumentException("minNotional cannot be negative");
        }
        return new Instrument(null, name, base, quote, priceStep, qtyStep, minNotional, perpetual);
    }

    public Instrument withId(Long newId) {
        Objects.requireNonNull(newId, "newId");
        return new Instrument(newId, name, base, quote, priceStep, qtyStep, minNotional, perpetual);
    }

    public static Instrument rehydrate(
        Long id,
        String name,
        String base,
        String quote,
        BigDecimal priceStep,
        BigDecimal qtyStep,
        BigDecimal minNotional,
        boolean perpetual
    ) {
        Objects.requireNonNull(id, "id");
        return new Instrument(id, name, base, quote, priceStep, qtyStep, minNotional, perpetual);
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String base() {
        return base;
    }

    public String quote() {
        return quote;
    }

    public BigDecimal priceStep() {
        return priceStep;
    }

    public BigDecimal qtyStep() {
        return qtyStep;
    }

    public BigDecimal minNotional() {
        return minNotional;
    }

    public boolean perpetual() {
        return perpetual;
    }

    public BigDecimal roundPrice(BigDecimal value) {
        Objects.requireNonNull(value, "value");
        return quantize(value, priceStep);
    }

    public BigDecimal roundQuantity(BigDecimal value) {
        Objects.requireNonNull(value, "value");
        return quantize(value, qtyStep);
    }

    private BigDecimal quantize(BigDecimal value, BigDecimal step) {
        BigDecimal steps = value.divide(step, 0, RoundingMode.DOWN);
        return steps.multiply(step);
    }
}
