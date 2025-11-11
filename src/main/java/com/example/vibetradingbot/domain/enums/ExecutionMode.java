package com.example.vibetradingbot.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Режим исполнения торговых операций.
 */
@Getter
@RequiredArgsConstructor
public enum ExecutionMode {

    PAPER("PAPER"),
    TESTNET("TESTNET"),
    LIVE("LIVE");

    private final String code;
}
