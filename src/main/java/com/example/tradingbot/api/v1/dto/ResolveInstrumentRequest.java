package com.example.tradingbot.api.v1.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResolveInstrumentRequest(
    @NotBlank String name,
    @NotBlank String base,
    @NotBlank String quote,
    @NotNull BigDecimal priceStep,
    @NotNull BigDecimal qtyStep,
    BigDecimal minNotional,
    boolean perpetual
) {
}
