package com.example.vibetradingbot.domain.enums;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

import com.example.vibetradingbot.exception.ServiceException;
import com.example.vibetradingbot.util.Constants;

/**
 * Канонические таймфреймы системы.
 */
public enum CanonicalTimeframe {
    M1(Duration.ofMinutes(1)),
    M5(Duration.ofMinutes(5)),
    M15(Duration.ofMinutes(15)),
    M30(Duration.ofMinutes(30)),
    H1(Duration.ofHours(1)),
    H4(Duration.ofHours(4)),
    D1(Duration.ofDays(1));

    private final Duration duration;

    CanonicalTimeframe(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }

    public static CanonicalTimeframe fromCode(String code) {
        if (Objects.isNull(code)) {
            throw new ServiceException(Constants.Errors.DOMAIN_VALIDATION_FAILED, Constants.Validation.NULL_TIMEFRAME);
        }
        return Arrays.stream(values())
            .filter(value -> Objects.equals(value.name(), code))
            .findFirst()
            .orElseThrow(() -> new ServiceException(Constants.Errors.DOMAIN_VALIDATION_FAILED, Constants.Validation.UNKNOWN_TIMEFRAME));
    }
}
