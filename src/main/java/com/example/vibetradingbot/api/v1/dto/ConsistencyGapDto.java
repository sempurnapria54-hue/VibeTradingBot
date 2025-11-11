package com.example.vibetradingbot.api.v1.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO пропуска данных.
 */
@Getter
@Builder
public class ConsistencyGapDto {

    private Instant fromUtc;
    private Instant toUtc;
}
