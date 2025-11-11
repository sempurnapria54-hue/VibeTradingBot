package com.example.vibetradingbot.domain.enums;

/**
 * Статус заявки в торговой системе.
 */
public enum OrderStatus {

    NEW,
    SUBMITTED,
    PARTIALLY_FILLED,
    FILLED,
    CANCELED,
    REJECTED,
    EXPIRED
}
