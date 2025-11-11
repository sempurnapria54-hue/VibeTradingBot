package com.example.vibetradingbot.api.v1.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Ответ на активацию параметров кворума.
 */
@Getter
@Builder
public class QuorumParamsActivateResponse {

    private final Long quorumParamsId;
    private final String version;
}
