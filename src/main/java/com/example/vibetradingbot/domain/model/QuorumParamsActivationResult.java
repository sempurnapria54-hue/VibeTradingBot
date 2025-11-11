package com.example.vibetradingbot.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Результат активации параметров кворума.
 */
@Getter
@Builder
public class QuorumParamsActivationResult {

    private final long quorumParamsId;
    private final String version;
}
