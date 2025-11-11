package com.example.vibetradingbot.api.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ с идентификатором запуска бэктеста.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestRunResponse {

    private Long runId;
}
