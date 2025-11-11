package com.example.vibetradingbot.persistence.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.vibetradingbot.domain.enums.BacktestSide;
import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.enums.ExitReason;
import com.example.vibetradingbot.domain.model.BacktestEquityPoint;
import com.example.vibetradingbot.domain.model.BacktestPerformanceAggregate;
import com.example.vibetradingbot.domain.model.BacktestRun;
import com.example.vibetradingbot.domain.model.BacktestTrade;
import com.example.vibetradingbot.domain.model.BacktestTradePage;
import com.example.vibetradingbot.util.Constants;

/**
 * Репозиторий операций бэктеста через jOOQ.
 */
@Repository
public class BacktestRepository {

    private static final Table<Record> BACKTEST_RUN_TABLE = DSL
        .table(DSL.name(Constants.Schemas.BACKTEST, Constants.Fields.BACKTEST_RUN));
    private static final Table<Record> BACKTEST_TRADE_TABLE = DSL
        .table(DSL.name(Constants.Schemas.BACKTEST, Constants.Fields.BACKTEST_TRADE));
    private static final Table<Record> BACKTEST_EQUITY_TABLE = DSL
        .table(DSL.name(Constants.Schemas.BACKTEST, Constants.Fields.BACKTEST_EQUITY_POINT));
    private static final Table<Record> PERFORMANCE_TABLE = DSL
        .table(DSL.name(Constants.Schemas.BACKTEST, Constants.Fields.PERFORMANCE_AGGREGATE));

    private static final Field<Long> RUN_ID_FIELD = DSL.field(DSL.name(Constants.Fields.RUN_ID), Long.class);
    private static final Field<Long> ID_FIELD = DSL.field(DSL.name(Constants.Fields.ID), Long.class);
    private static final Field<java.util.UUID> EXCHANGE_INSTRUMENT_ID_FIELD = DSL
        .field(DSL.name(Constants.Fields.EXCHANGE_INSTRUMENT_ID), java.util.UUID.class);
    private static final Field<String> TIMEFRAME_FIELD = DSL
        .field(DSL.name(Constants.Fields.TIMEFRAME_CANONICAL), String.class);
    private static final Field<java.time.Instant> FROM_FIELD = DSL
        .field(DSL.name(Constants.Fields.FROM_UTC), java.time.Instant.class);
    private static final Field<java.time.Instant> TO_FIELD = DSL
        .field(DSL.name(Constants.Fields.TO_UTC), java.time.Instant.class);
    private static final Field<String[]> SIGNAL_TYPES_FIELD = DSL
        .field(DSL.name(Constants.Fields.SIGNAL_TYPES), String[].class);
    private static final Field<Long> SIGNAL_PARAMS_FIELD = DSL
        .field(DSL.name(Constants.Fields.SIGNAL_PARAMS_ID), Long.class);
    private static final Field<Long> QUORUM_PARAMS_FIELD = DSL
        .field(DSL.name(Constants.Fields.QUORUM_PARAMS_ID), Long.class);
    private static final Field<Long> RISK_PARAMS_FIELD = DSL
        .field(DSL.name(Constants.Fields.RISK_PARAMS_ID), Long.class);
    private static final Field<Long> EXCHANGE_PARAMS_FIELD = DSL
        .field(DSL.name(Constants.Fields.EXCHANGE_PARAMS_ID), Long.class);
    private static final Field<Long> BACKTEST_PARAMS_FIELD = DSL
        .field(DSL.name(Constants.Fields.BACKTEST_PARAMS_ID), Long.class);
    private static final Field<JSONB> PARAMS_JSON_FIELD = DSL
        .field(DSL.name(Constants.Fields.PARAMS_JSON), JSONB.class);
    private static final Field<java.time.Instant> CREATED_AT_FIELD = DSL
        .field(DSL.name(Constants.Fields.CREATED_AT), java.time.Instant.class);

    private static final Field<java.time.Instant> ENTRY_TS_FIELD = DSL
        .field(DSL.name(Constants.Fields.ENTRY_TS), java.time.Instant.class);
    private static final Field<java.time.Instant> EXIT_TS_FIELD = DSL
        .field(DSL.name(Constants.Fields.EXIT_TS), java.time.Instant.class);
    private static final Field<String> SIDE_FIELD = DSL.field(DSL.name(Constants.Fields.SIDE), String.class);
    private static final Field<BigDecimal> ENTRY_PRICE_FIELD = DSL
        .field(DSL.name(Constants.Fields.ENTRY_PRICE), BigDecimal.class);
    private static final Field<BigDecimal> EXIT_PRICE_FIELD = DSL
        .field(DSL.name(Constants.Fields.EXIT_PRICE), BigDecimal.class);
    private static final Field<BigDecimal> PNL_FIELD = DSL.field(DSL.name(Constants.Fields.PNL_R), BigDecimal.class);
    private static final Field<BigDecimal> FEES_FIELD = DSL.field(DSL.name(Constants.Fields.FEES), BigDecimal.class);
    private static final Field<BigDecimal> SLIPPAGE_FIELD = DSL
        .field(DSL.name(Constants.Fields.SLIPPAGE), BigDecimal.class);
    private static final Field<BigDecimal> FUNDING_FIELD = DSL
        .field(DSL.name(Constants.Fields.FUNDING), BigDecimal.class);
    private static final Field<BigDecimal> MAE_FIELD = DSL.field(DSL.name(Constants.Fields.MAE_R), BigDecimal.class);
    private static final Field<BigDecimal> MFE_FIELD = DSL.field(DSL.name(Constants.Fields.MFE_R), BigDecimal.class);
    private static final Field<String> WHICH_HIT_FIELD = DSL
        .field(DSL.name(Constants.Fields.WHICH_HIT_FIRST), String.class);
    private static final Field<Integer> TIME_TO_EVENT_FIELD = DSL
        .field(DSL.name(Constants.Fields.TIME_TO_EVENT_BARS), Integer.class);
    private static final Field<JSONB> REASON_FIELD = DSL.field(DSL.name(Constants.Fields.REASON), JSONB.class);
    private static final Field<Long> SIGNAL_ID_FIELD = DSL.field(DSL.name(Constants.Fields.SIGNAL_ID), Long.class);

    private static final Field<java.time.Instant> EQUITY_TS_FIELD = DSL
        .field(DSL.name(Constants.Fields.TS_UTC), java.time.Instant.class);
    private static final Field<BigDecimal> EQUITY_VALUE_FIELD = DSL
        .field(DSL.name(Constants.Fields.EQUITY_R), BigDecimal.class);

    private static final Field<Integer> TRADES_FIELD = DSL.field(DSL.name(Constants.Fields.TRADES), Integer.class);
    private static final Field<BigDecimal> WINRATE_FIELD = DSL.field(DSL.name(Constants.Fields.WINRATE), BigDecimal.class);
    private static final Field<BigDecimal> PROFIT_FACTOR_FIELD = DSL
        .field(DSL.name(Constants.Fields.PROFIT_FACTOR), BigDecimal.class);
    private static final Field<BigDecimal> EXPECTANCY_FIELD = DSL
        .field(DSL.name(Constants.Fields.EXPECTANCY_R), BigDecimal.class);
    private static final Field<BigDecimal> MAX_DD_FIELD = DSL
        .field(DSL.name(Constants.Fields.MAX_DRAWDOWN_R), BigDecimal.class);
    private static final Field<BigDecimal> SHARPE_FIELD = DSL.field(DSL.name(Constants.Fields.SHARPE), BigDecimal.class);
    private static final Field<BigDecimal> SORTINO_FIELD = DSL.field(DSL.name(Constants.Fields.SORTINO), BigDecimal.class);

    private final DSLContext dsl;

    public BacktestRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Transactional
    public long insertRun(BacktestRun run) {
        Long runId = dsl.insertInto(BACKTEST_RUN_TABLE)
            .set(EXCHANGE_INSTRUMENT_ID_FIELD, run.getExchangeInstrumentId())
            .set(TIMEFRAME_FIELD, run.getTimeframe().name())
            .set(FROM_FIELD, run.getFromUtc())
            .set(TO_FIELD, run.getToUtc())
            .set(SIGNAL_TYPES_FIELD, run.getSignalTypes().toArray(new String[0]))
            .set(SIGNAL_PARAMS_FIELD, run.getSignalParamsId())
            .set(QUORUM_PARAMS_FIELD, run.getQuorumParamsId())
            .set(RISK_PARAMS_FIELD, run.getRiskParamsId())
            .set(EXCHANGE_PARAMS_FIELD, run.getExchangeParamsId())
            .set(BACKTEST_PARAMS_FIELD, run.getBacktestParamsId())
            .set(PARAMS_JSON_FIELD, JSONB.valueOf(run.getParamsSnapshot()))
            .set(CREATED_AT_FIELD, Objects.requireNonNullElseGet(run.getCreatedAt(), Instant::now))
            .returning(ID_FIELD)
            .fetchOne(ID_FIELD);
        return runId == null ? -1 : runId;
    }

    @Transactional(readOnly = true)
    public Optional<Long> findExistingRun(CanonicalTimeframe timeframe, java.util.UUID exchangeInstrumentId,
            Instant fromUtc, Instant toUtc, Long backtestParamsId) {
        Record record = dsl.select(ID_FIELD)
            .from(BACKTEST_RUN_TABLE)
            .where(TIMEFRAME_FIELD.eq(timeframe.name())
                .and(EXCHANGE_INSTRUMENT_ID_FIELD.eq(exchangeInstrumentId))
                .and(FROM_FIELD.eq(fromUtc))
                .and(TO_FIELD.eq(toUtc))
                .and(BACKTEST_PARAMS_FIELD.eq(backtestParamsId)))
            .fetchOne();
        if (record == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(record.get(ID_FIELD));
    }

    @Transactional(readOnly = true)
    public Optional<BacktestRun> findRunById(long runId) {
        Record record = dsl.select(ID_FIELD, EXCHANGE_INSTRUMENT_ID_FIELD, TIMEFRAME_FIELD, FROM_FIELD, TO_FIELD,
                SIGNAL_TYPES_FIELD, SIGNAL_PARAMS_FIELD, QUORUM_PARAMS_FIELD, RISK_PARAMS_FIELD, EXCHANGE_PARAMS_FIELD,
                BACKTEST_PARAMS_FIELD, PARAMS_JSON_FIELD, CREATED_AT_FIELD)
            .from(BACKTEST_RUN_TABLE)
            .where(ID_FIELD.eq(runId))
            .fetchOne();
        if (record == null) {
            return Optional.empty();
        }
        return Optional.of(mapRun(record));
    }

    @Transactional
    public void insertPerformance(BacktestPerformanceAggregate aggregate) {
        dsl.insertInto(PERFORMANCE_TABLE)
            .set(RUN_ID_FIELD, aggregate.getRunId())
            .set(TRADES_FIELD, aggregate.getTrades())
            .set(WINRATE_FIELD, aggregate.getWinrate())
            .set(PROFIT_FACTOR_FIELD, aggregate.getProfitFactor())
            .set(EXPECTANCY_FIELD, aggregate.getExpectancyR())
            .set(MAX_DD_FIELD, aggregate.getMaxDrawdownR())
            .set(SHARPE_FIELD, aggregate.getSharpe())
            .set(SORTINO_FIELD, aggregate.getSortino())
            .onConflict(RUN_ID_FIELD)
            .doUpdate()
            .set(TRADES_FIELD, aggregate.getTrades())
            .set(WINRATE_FIELD, aggregate.getWinrate())
            .set(PROFIT_FACTOR_FIELD, aggregate.getProfitFactor())
            .set(EXPECTANCY_FIELD, aggregate.getExpectancyR())
            .set(MAX_DD_FIELD, aggregate.getMaxDrawdownR())
            .set(SHARPE_FIELD, aggregate.getSharpe())
            .set(SORTINO_FIELD, aggregate.getSortino())
            .execute();
    }

    @Transactional
    public void insertEquityPoints(List<BacktestEquityPoint> points) {
        if (points == null || points.isEmpty()) {
            return;
        }
        for (BacktestEquityPoint point : points) {
            dsl.insertInto(BACKTEST_EQUITY_TABLE)
                .set(RUN_ID_FIELD, point.getRunId())
                .set(EQUITY_TS_FIELD, point.getTimestamp())
                .set(EQUITY_VALUE_FIELD, point.getEquityR())
                .execute();
        }
    }

    @Transactional
    public void insertTrades(List<BacktestTrade> trades) {
        if (trades == null || trades.isEmpty()) {
            return;
        }
        for (BacktestTrade trade : trades) {
            dsl.insertInto(BACKTEST_TRADE_TABLE)
                .set(RUN_ID_FIELD, trade.getRunId())
                .set(SIGNAL_ID_FIELD, trade.getSignalId())
                .set(ENTRY_TS_FIELD, trade.getEntryTimestamp())
                .set(EXIT_TS_FIELD, trade.getExitTimestamp())
                .set(SIDE_FIELD, trade.getSide() == null ? null : trade.getSide().name())
                .set(ENTRY_PRICE_FIELD, trade.getEntryPrice())
                .set(EXIT_PRICE_FIELD, trade.getExitPrice())
                .set(PNL_FIELD, trade.getPnlR())
                .set(FEES_FIELD, trade.getFees())
                .set(SLIPPAGE_FIELD, trade.getSlippage())
                .set(FUNDING_FIELD, trade.getFunding())
                .set(MAE_FIELD, trade.getMaeR())
                .set(MFE_FIELD, trade.getMfeR())
                .set(WHICH_HIT_FIELD, trade.getExitReason() == null ? null : trade.getExitReason().name())
                .set(TIME_TO_EVENT_FIELD, trade.getTimeToEventBars())
                .set(REASON_FIELD, JSONB.valueOf(trade.getReason() == null ? "{}" : trade.getReason()))
                .execute();
        }
    }

    @Transactional(readOnly = true)
    public Optional<BacktestPerformanceAggregate> findPerformance(long runId) {
        Record record = dsl.select(RUN_ID_FIELD, TRADES_FIELD, WINRATE_FIELD, PROFIT_FACTOR_FIELD, EXPECTANCY_FIELD,
                MAX_DD_FIELD, SHARPE_FIELD, SORTINO_FIELD)
            .from(PERFORMANCE_TABLE)
            .where(RUN_ID_FIELD.eq(runId))
            .fetchOne();
        if (record == null) {
            return Optional.empty();
        }
        BacktestPerformanceAggregate aggregate = BacktestPerformanceAggregate.create(record.get(RUN_ID_FIELD),
                record.get(TRADES_FIELD), record.get(WINRATE_FIELD), record.get(PROFIT_FACTOR_FIELD),
                record.get(EXPECTANCY_FIELD), record.get(MAX_DD_FIELD), record.get(SHARPE_FIELD),
                record.get(SORTINO_FIELD));
        return Optional.of(aggregate);
    }

    @Transactional(readOnly = true)
    public List<BacktestEquityPoint> findEquity(long runId) {
        List<Record> records = dsl.select(ID_FIELD, RUN_ID_FIELD, EQUITY_TS_FIELD, EQUITY_VALUE_FIELD)
            .from(BACKTEST_EQUITY_TABLE)
            .where(RUN_ID_FIELD.eq(runId))
            .orderBy(EQUITY_TS_FIELD.asc())
            .fetch();
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        List<BacktestEquityPoint> points = new ArrayList<>();
        for (Record record : records) {
            points.add(BacktestEquityPoint.builder()
                .id(record.get(ID_FIELD))
                .runId(record.get(RUN_ID_FIELD))
                .timestamp(record.get(EQUITY_TS_FIELD))
                .equityR(record.get(EQUITY_VALUE_FIELD))
                .build());
        }
        return points;
    }

    @Transactional(readOnly = true)
    public BacktestTradePage findTrades(long runId, int page, int size) {
        int safeSize = Math.max(size, 1);
        int safePage = Math.max(page, 0);
        int offset = safePage * safeSize;
        List<Record> records = dsl.select(ID_FIELD, RUN_ID_FIELD, SIGNAL_ID_FIELD, ENTRY_TS_FIELD, EXIT_TS_FIELD,
                SIDE_FIELD, ENTRY_PRICE_FIELD, EXIT_PRICE_FIELD, PNL_FIELD, FEES_FIELD, SLIPPAGE_FIELD, FUNDING_FIELD,
                MAE_FIELD, MFE_FIELD, WHICH_HIT_FIELD, TIME_TO_EVENT_FIELD, REASON_FIELD)
            .from(BACKTEST_TRADE_TABLE)
            .where(RUN_ID_FIELD.eq(runId))
            .orderBy(ENTRY_TS_FIELD.asc())
            .limit(safeSize)
            .offset(offset)
            .fetch();
        List<BacktestTrade> trades = new ArrayList<>();
        for (Record record : records) {
            trades.add(mapTrade(record));
        }
        Integer total = dsl.fetchCount(dsl.selectFrom(BACKTEST_TRADE_TABLE).where(RUN_ID_FIELD.eq(runId)));
        return BacktestTradePage.create(trades, total, safePage, safeSize);
    }

    private BacktestRun mapRun(Record record) {
        String[] rawSignalTypes = record.get(SIGNAL_TYPES_FIELD);
        List<String> signalTypes = new ArrayList<>();
        if (rawSignalTypes != null) {
            Collections.addAll(signalTypes, rawSignalTypes);
        }
        return BacktestRun.builder()
            .id(record.get(ID_FIELD))
            .exchangeInstrumentId(record.get(EXCHANGE_INSTRUMENT_ID_FIELD))
            .timeframe(CanonicalTimeframe.valueOf(record.get(TIMEFRAME_FIELD)))
            .fromUtc(record.get(FROM_FIELD))
            .toUtc(record.get(TO_FIELD))
            .signalTypes(signalTypes)
            .signalParamsId(record.get(SIGNAL_PARAMS_FIELD))
            .quorumParamsId(record.get(QUORUM_PARAMS_FIELD))
            .riskParamsId(record.get(RISK_PARAMS_FIELD))
            .exchangeParamsId(record.get(EXCHANGE_PARAMS_FIELD))
            .backtestParamsId(record.get(BACKTEST_PARAMS_FIELD))
            .paramsSnapshot(record.get(PARAMS_JSON_FIELD) == null ? "{}" : record.get(PARAMS_JSON_FIELD).data())
            .createdAt(record.get(CREATED_AT_FIELD))
            .build();
    }

    private BacktestTrade mapTrade(Record record) {
        BacktestSide side = null;
        if (record.get(SIDE_FIELD) != null) {
            side = BacktestSide.valueOf(record.get(SIDE_FIELD));
        }
        ExitReason exitReason = null;
        if (record.get(WHICH_HIT_FIELD) != null) {
            exitReason = ExitReason.valueOf(record.get(WHICH_HIT_FIELD));
        }
        return BacktestTrade.builder()
            .id(record.get(ID_FIELD))
            .runId(record.get(RUN_ID_FIELD))
            .signalId(record.get(SIGNAL_ID_FIELD))
            .entryTimestamp(record.get(ENTRY_TS_FIELD))
            .exitTimestamp(record.get(EXIT_TS_FIELD))
            .side(side)
            .entryPrice(record.get(ENTRY_PRICE_FIELD))
            .exitPrice(record.get(EXIT_PRICE_FIELD))
            .pnlR(record.get(PNL_FIELD))
            .fees(record.get(FEES_FIELD))
            .slippage(record.get(SLIPPAGE_FIELD))
            .funding(record.get(FUNDING_FIELD))
            .maeR(record.get(MAE_FIELD))
            .mfeR(record.get(MFE_FIELD))
            .exitReason(exitReason)
            .timeToEventBars(record.get(TIME_TO_EVENT_FIELD))
            .reason(record.get(REASON_FIELD) == null ? "{}" : record.get(REASON_FIELD).data())
            .build();
    }
}
