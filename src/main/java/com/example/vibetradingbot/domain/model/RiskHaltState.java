package com.example.vibetradingbot.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.example.vibetradingbot.domain.enums.RiskHaltScope;
import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Флаг остановки торговли.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
public class RiskHaltState {

    private RiskHaltScope scope;
    private Long exchangeInstrumentId;
    private boolean halted;
    private String reason;
    private Instant updatedAt;

    @Builder
    private RiskHaltState(RiskHaltScope scope,
        Long exchangeInstrumentId,
        boolean halted,
        String reason,
        Instant updatedAt) {
        this.scope = Objects.requireNonNull(scope, Constants.Validation.NULL_IDENTIFIER);
        this.exchangeInstrumentId = exchangeInstrumentId;
        this.halted = halted;
        this.reason = reason;
        this.updatedAt = Objects.requireNonNull(updatedAt, Constants.Validation.NULL_IDENTIFIER);
    }

    public static RiskHaltState create(RiskHaltScope scope,
        Long exchangeInstrumentId,
        boolean halted,
        String reason,
        Instant updatedAt) {
        return RiskHaltState.builder()
            .scope(scope)
            .exchangeInstrumentId(exchangeInstrumentId)
            .halted(halted)
            .reason(reason)
            .updatedAt(updatedAt)
            .build();
    }
}
