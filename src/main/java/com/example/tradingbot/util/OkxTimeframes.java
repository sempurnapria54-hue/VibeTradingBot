package com.example.tradingbot.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public enum OkxTimeframes {
    ONE_MINUTE("1 minute", "1m"),
    THREE_MINUTES("3 minutes", "3m"),
    FIVE_MINUTES("5 minutes", "5m"),
    FIFTEEN_MINUTES("15 minutes", "15m"),
    ONE_HOUR("1 hour", "1H"),
    TWO_HOURS("2 hours", "2H"),
    FOUR_HOURS("4 hours", "4H"),
    ONE_DAY("1 day", "1D");

    private final String canonical;
    private final String code;

    OkxTimeframes(String canonical, String code) {
        this.canonical = canonical;
        this.code = code;
    }

    public String canonical() {
        return canonical;
    }

    public String code() {
        return code;
    }

    public static List<String> canonicalValues() {
        return Arrays.stream(values())
            .map(OkxTimeframes::canonical)
            .collect(Collectors.toList());
    }

    public static Map<String, String> canonicalToCode() {
        return Arrays.stream(values())
            .collect(Collectors.toMap(OkxTimeframes::canonical, OkxTimeframes::code));
    }

    public static Optional<OkxTimeframes> fromCanonical(String canonical) {
        return Arrays.stream(values())
            .filter(value -> value.canonical.equalsIgnoreCase(canonical))
            .findFirst();
    }

    public static Optional<OkxTimeframes> fromCode(String code) {
        return Arrays.stream(values())
            .filter(value -> value.code.equalsIgnoreCase(code))
            .findFirst();
    }

    public static String normalizeCanonical(String canonical) {
        return fromCanonical(canonical)
            .map(OkxTimeframes::canonical)
            .orElseThrow(() -> new IllegalArgumentException("Unsupported OKX timeframe: " + canonical));
    }

    public static String normalizeCode(String code) {
        return fromCode(code)
            .map(OkxTimeframes::code)
            .orElseThrow(() -> new IllegalArgumentException("Unsupported OKX timeframe code: " + code));
    }

    public static boolean matchesCanonical(String value) {
        return Arrays.stream(values())
            .anyMatch(frame -> frame.canonical.equals(value));
    }

    public static String canonicalOrThrow(String value) {
        return Arrays.stream(values())
            .filter(frame -> frame.canonical.equals(value))
            .findFirst()
            .map(OkxTimeframes::canonical)
            .orElseThrow(() -> new IllegalArgumentException("Unknown canonical timeframe: " + value));
    }
}
