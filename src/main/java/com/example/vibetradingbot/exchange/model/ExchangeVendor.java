package com.example.vibetradingbot.exchange.model;

/**
 * Поддерживаемые биржевые коннекторы.
 */
public enum ExchangeVendor {
    OKX;

    public static ExchangeVendor fromCode(String code) {
        return ExchangeVendor.valueOf(code.toUpperCase());
    }
}
