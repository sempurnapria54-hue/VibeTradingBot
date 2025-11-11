package com.example.vibetradingbot.api.v1.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * Ответ на запрос последних решений кворума.
 */
@Getter
@Builder
public class QuorumLatestResponse {

    private final List<QuorumDecisionDto> decisions;
}
