package com.example.vibetradingbot.api.v1.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO отчёта по запуску бэктеста.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestReportResponse {

    private Long runId;
    private UUID exchangeInstrumentId;
    private String timeframe;
    private Instant fromUtc;
    private Instant toUtc;
    private List<String> signalTypes;
    private Long signalParamsId;
    private Long quorumParamsId;
    private Long riskParamsId;
    private Long exchangeParamsId;
    private Long backtestParamsId;
    private Map<String, Object> paramsSnapshot;
    private BacktestPerformanceDto performance;
}
