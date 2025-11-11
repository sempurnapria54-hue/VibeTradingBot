package com.example.vibetradingbot.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.model.Candle;
import com.example.vibetradingbot.domain.model.CandleCoverage;
import com.example.vibetradingbot.persistence.mapper.DomainToPersistenceMapper;
import com.example.vibetradingbot.persistence.mapper.PersistenceToDomainMapper;
import com.example.vibetradingbot.persistence.model.CandleCoverageRecordModel;
import com.example.vibetradingbot.persistence.model.CandleRecordModel;
import com.example.vibetradingbot.util.Constants;

/**
 * Репозиторий операций над свечами через jOOQ.
 */
@Repository
public class CandleRepository {

    private static final Table<Record> CANDLE_TABLE = DSL.table(DSL.name(Constants.Schemas.CANDLES, Constants.Fields.CANDLE));
    private static final Table<Record> COVERAGE_TABLE = DSL.table(DSL.name(Constants.Schemas.COVERAGE, Constants.Fields.COVERAGE));

    private static final Field<UUID> ID_FIELD = DSL.field(DSL.name(Constants.Fields.ID), UUID.class);
    private static final Field<UUID> EXCHANGE_INSTRUMENT_FIELD = DSL.field(DSL.name(Constants.Fields.EXCHANGE_INSTRUMENT_ID), UUID.class);
    private static final Field<String> TIMEFRAME_FIELD = DSL.field(DSL.name(Constants.Fields.TIMEFRAME), String.class);
    private static final Field<java.time.Instant> OPEN_TIME_FIELD = DSL.field(DSL.name(Constants.Fields.OPEN_TIME_UTC), java.time.Instant.class);
    private static final Field<java.time.Instant> CLOSE_TIME_FIELD = DSL.field(DSL.name(Constants.Fields.CLOSE_TIME_UTC), java.time.Instant.class);
    private static final Field<java.time.Instant> COVERAGE_END_FIELD = DSL.field(DSL.name(Constants.Fields.COVERAGE_END_UTC), java.time.Instant.class);
    private static final Field<java.math.BigDecimal> OPEN_PRICE_FIELD = DSL.field(DSL.name(Constants.Fields.OPEN_PRICE), java.math.BigDecimal.class);
    private static final Field<java.math.BigDecimal> CLOSE_PRICE_FIELD = DSL.field(DSL.name(Constants.Fields.CLOSE_PRICE), java.math.BigDecimal.class);
    private static final Field<java.math.BigDecimal> HIGH_PRICE_FIELD = DSL.field(DSL.name(Constants.Fields.HIGH_PRICE), java.math.BigDecimal.class);
    private static final Field<java.math.BigDecimal> LOW_PRICE_FIELD = DSL.field(DSL.name(Constants.Fields.LOW_PRICE), java.math.BigDecimal.class);
    private static final Field<java.math.BigDecimal> VOLUME_FIELD = DSL.field(DSL.name(Constants.Fields.VOLUME), java.math.BigDecimal.class);
    private static final Field<Long> TRADES_COUNT_FIELD = DSL.field(DSL.name(Constants.Fields.TRADES_COUNT), Long.class);
    private static final Field<java.time.Instant> COVERAGE_START_FIELD = DSL.field(DSL.name(Constants.Fields.COVERAGE_START_UTC), java.time.Instant.class);
    private static final Field<Boolean> COMPLETE_FIELD = DSL.field(DSL.name(Constants.Fields.IS_COMPLETE), Boolean.class);
    private static final Field<java.time.Instant> UPDATED_AT_FIELD = DSL.field(DSL.name(Constants.Fields.UPDATED_AT), java.time.Instant.class);

    private final DSLContext dsl;
    private final DomainToPersistenceMapper domainToPersistenceMapper;
    private final PersistenceToDomainMapper persistenceToDomainMapper;

    public CandleRepository(DSLContext dsl, DomainToPersistenceMapper domainToPersistenceMapper,
            PersistenceToDomainMapper persistenceToDomainMapper) {
        this.dsl = dsl;
        this.domainToPersistenceMapper = domainToPersistenceMapper;
        this.persistenceToDomainMapper = persistenceToDomainMapper;
    }

    @Transactional
    public void upsertCandle(Candle candle) {
        CandleRecordModel record = domainToPersistenceMapper.toRecord(candle);
        dsl.insertInto(CANDLE_TABLE)
            .set(ID_FIELD, record.getId())
            .set(EXCHANGE_INSTRUMENT_FIELD, record.getExchangeInstrumentId())
            .set(TIMEFRAME_FIELD, record.getTimeframe())
            .set(OPEN_TIME_FIELD, record.getOpenTimeUtc())
            .set(CLOSE_TIME_FIELD, record.getCloseTimeUtc())
            .set(COVERAGE_END_FIELD, record.getCoverageEndUtc())
            .set(OPEN_PRICE_FIELD, record.getOpenPrice())
            .set(CLOSE_PRICE_FIELD, record.getClosePrice())
            .set(HIGH_PRICE_FIELD, record.getHighPrice())
            .set(LOW_PRICE_FIELD, record.getLowPrice())
            .set(VOLUME_FIELD, record.getVolume())
            .set(TRADES_COUNT_FIELD, record.getTradesCount())
            .onConflict(ID_FIELD)
            .doUpdate()
            .set(CLOSE_PRICE_FIELD, record.getClosePrice())
            .set(HIGH_PRICE_FIELD, record.getHighPrice())
            .set(LOW_PRICE_FIELD, record.getLowPrice())
            .set(VOLUME_FIELD, record.getVolume())
            .set(TRADES_COUNT_FIELD, record.getTradesCount())
            .set(COVERAGE_END_FIELD, record.getCoverageEndUtc())
            .execute();
    }

    @Transactional
    public void upsertCoverage(CandleCoverage coverage) {
        CandleCoverageRecordModel record = domainToPersistenceMapper.toRecord(coverage);
        dsl.insertInto(COVERAGE_TABLE)
            .set(ID_FIELD, record.getId())
            .set(EXCHANGE_INSTRUMENT_FIELD, record.getExchangeInstrumentId())
            .set(TIMEFRAME_FIELD, record.getTimeframe())
            .set(COVERAGE_START_FIELD, record.getCoverageStartUtc())
            .set(COVERAGE_END_FIELD, record.getCoverageEndUtc())
            .set(COMPLETE_FIELD, record.getComplete())
            .set(UPDATED_AT_FIELD, record.getUpdatedAt())
            .onConflict(EXCHANGE_INSTRUMENT_FIELD, TIMEFRAME_FIELD)
            .doUpdate()
            .set(COVERAGE_START_FIELD, record.getCoverageStartUtc())
            .set(COVERAGE_END_FIELD, record.getCoverageEndUtc())
            .set(COMPLETE_FIELD, record.getComplete())
            .set(UPDATED_AT_FIELD, record.getUpdatedAt())
            .execute();
    }

    @Transactional(readOnly = true)
    public Optional<CandleCoverage> findCoverage(UUID exchangeInstrumentId, CanonicalTimeframe timeframe) {
        Record result = dsl.select(ID_FIELD, EXCHANGE_INSTRUMENT_FIELD, TIMEFRAME_FIELD, COVERAGE_START_FIELD,
                COVERAGE_END_FIELD, COMPLETE_FIELD, UPDATED_AT_FIELD)
            .from(COVERAGE_TABLE)
            .where(EXCHANGE_INSTRUMENT_FIELD.eq(exchangeInstrumentId)
                .and(TIMEFRAME_FIELD.eq(timeframe.name())))
            .fetchOne();
        if (result == null) {
            return Optional.empty();
        }
        CandleCoverageRecordModel recordModel = new CandleCoverageRecordModel();
        recordModel.setId(result.get(ID_FIELD));
        recordModel.setExchangeInstrumentId(result.get(EXCHANGE_INSTRUMENT_FIELD));
        recordModel.setTimeframe(result.get(TIMEFRAME_FIELD));
        recordModel.setCoverageStartUtc(result.get(COVERAGE_START_FIELD));
        recordModel.setCoverageEndUtc(result.get(COVERAGE_END_FIELD));
        recordModel.setComplete(result.get(COMPLETE_FIELD));
        recordModel.setUpdatedAt(result.get(UPDATED_AT_FIELD));
        return Optional.of(persistenceToDomainMapper.toCoverage(recordModel));
    }
}
