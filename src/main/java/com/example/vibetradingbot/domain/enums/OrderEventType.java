package com.example.vibetradingbot.domain.enums;

/**
 * Тип события заявки.
 */
public enum OrderEventType {

    REQUESTED,
    SUBMITTED,
    ACCEPTED,
    PARTIALLY_FILLED,
    FILLED,
    CANCELED,
    REJECTED,
    EXPIRED,
    MODIFIED
}
