package com.example.tradingbot.persistence.repository;

import com.example.tradingbot.config.TimeframeProperties;
import com.example.tradingbot.domain.model.ExchangeInstrument;
import com.example.tradingbot.domain.model.HistoryGroup;
import com.example.tradingbot.mapping.PersistenceToDomainMapper;
import com.example.tradingbot.persistence.model.HistoryGroupEntity;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class HistoryGroupRepository {

    private static final Table<Record> TABLE = DSL.table("history_group");
    private static final Field<Long> ID = DSL.field("id", Long.class);
    private static final Field<Long> EXCHANGE_INSTRUMENT_ID = DSL.field("exchange_instrument_id", Long.class);
    private static final Field<String> TIMEFRAME = DSL.field("timeframe_canonical", String.class);
    private static final Field<Instant> COVERAGE_START = DSL.field("coverage_start_utc", Instant.class);

    private final DSLContext dsl;
    private final PersistenceToDomainMapper mapper;
    private final Set<String> canonicalTimeframes;

    public HistoryGroupRepository(DSLContext dsl, PersistenceToDomainMapper mapper, TimeframeProperties properties) {
        this.dsl = dsl;
        this.mapper = mapper;
        this.canonicalTimeframes = properties.getCanonical() != null ? Set.copyOf(properties.getCanonical()) : Set.of();
    }

    public HistoryGroup upsert(ExchangeInstrument exchangeInstrument, String timeframeCanonical, Instant coverageStart) {
        Optional<HistoryGroup> existing = findByExchangeInstrumentAndTimeframe(exchangeInstrument, timeframeCanonical);
        if (existing.isPresent()) {
            return existing.get();
        }
        var insert = dsl.insertInto(TABLE)
            .set(EXCHANGE_INSTRUMENT_ID, exchangeInstrument.id())
            .set(TIMEFRAME, timeframeCanonical);
        if (coverageStart != null) {
            insert.set(COVERAGE_START, coverageStart);
        }
        Long id = insert.returningResult(ID).fetchOne(ID);
        HistoryGroupEntity entity = new HistoryGroupEntity();
        entity.setId(id);
        entity.setExchangeInstrumentId(exchangeInstrument.id());
        entity.setTimeframeCanonical(timeframeCanonical);
        entity.setCoverageStartUtc(coverageStart);
        return mapper.toDomain(entity, exchangeInstrument, canonicalTimeframes);
    }

    public Optional<HistoryGroup> findByExchangeInstrumentAndTimeframe(ExchangeInstrument exchangeInstrument, String timeframeCanonical) {
        return dsl.selectFrom(TABLE)
            .where(EXCHANGE_INSTRUMENT_ID.eq(exchangeInstrument.id()).and(TIMEFRAME.eq(timeframeCanonical)))
            .fetchOptional(record -> {
                HistoryGroupEntity entity = new HistoryGroupEntity();
                entity.setId(record.get(ID));
                entity.setExchangeInstrumentId(record.get(EXCHANGE_INSTRUMENT_ID));
                entity.setTimeframeCanonical(record.get(TIMEFRAME));
                entity.setCoverageStartUtc(record.get(COVERAGE_START));
                return mapper.toDomain(entity, exchangeInstrument, canonicalTimeframes);
            });
    }
}
