package com.example.vibetradingbot.domain.model;

import java.util.Objects;

import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Результат проверки рисков.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
public class RiskCheckResult {

    private boolean allowed;
    private String reason;

    @Builder
    private RiskCheckResult(boolean allowed, String reason) {
        this.allowed = allowed;
        this.reason = reason;
    }

    public static RiskCheckResult allow() {
        return RiskCheckResult.builder()
            .allowed(true)
            .build();
    }

    public static RiskCheckResult deny(String reason) {
        return RiskCheckResult.builder()
            .allowed(false)
            .reason(Objects.requireNonNull(reason, Constants.Validation.NULL_IDENTIFIER))
            .build();
    }
}
