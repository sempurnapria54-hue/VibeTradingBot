package com.example.vibetradingbot.domain.model;

import java.util.Objects;

import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Отчет по запуску бэктеста: конфигурация + агрегаты.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class BacktestReport {

    private BacktestRun run;
    private BacktestPerformanceAggregate aggregate;

    @Builder
    private BacktestReport(BacktestRun run, BacktestPerformanceAggregate aggregate) {
        this.run = Objects.requireNonNull(run, Constants.Validation.CONFIGURATION_ERROR);
        this.aggregate = aggregate;
    }

    public static BacktestReport create(BacktestRun run, BacktestPerformanceAggregate aggregate) {
        return BacktestReport.builder()
            .run(run)
            .aggregate(aggregate)
            .build();
    }
}
