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

import com.example.vibetradingbot.domain.model.ExchangeInstrument;
import com.example.vibetradingbot.persistence.mapper.PersistenceToDomainMapper;
import com.example.vibetradingbot.persistence.model.ExchangeInstrumentRecordModel;
import com.example.vibetradingbot.util.Constants;

/**
 * Репозиторий связок биржа-инструмент.
 */
@Repository
public class ExchangeInstrumentRepository {

    private static final Table<Record> EXCHANGE_INSTRUMENT_TABLE = DSL.table(DSL.name(Constants.Schemas.EXCHANGE_INSTRUMENT, Constants.Fields.EXCHANGE_INSTRUMENT));

    private static final Field<UUID> ID_FIELD = DSL.field(DSL.name(Constants.Fields.ID), UUID.class);
    private static final Field<UUID> EXCHANGE_ID_FIELD = DSL.field(DSL.name(Constants.Fields.EXCHANGE_ID), UUID.class);
    private static final Field<UUID> INSTRUMENT_ID_FIELD = DSL.field(DSL.name(Constants.Fields.INSTRUMENT_ID), UUID.class);
    private static final Field<String> EXCHANGE_SYMBOL_FIELD = DSL.field(DSL.name(Constants.Fields.EXCHANGE_SYMBOL), String.class);
    private static final Field<java.math.BigDecimal> MIN_TRADE_QUANTITY_FIELD = DSL.field(DSL.name(Constants.Fields.MIN_TRADE_QUANTITY), java.math.BigDecimal.class);
    private static final Field<java.math.BigDecimal> TICK_SIZE_FIELD = DSL.field(DSL.name(Constants.Fields.TICK_SIZE), java.math.BigDecimal.class);
    private static final Field<java.math.BigDecimal> MAKER_FEE_FIELD = DSL.field(DSL.name(Constants.Fields.MAKER_FEE_RATE), java.math.BigDecimal.class);
    private static final Field<java.math.BigDecimal> TAKER_FEE_FIELD = DSL.field(DSL.name(Constants.Fields.TAKER_FEE_RATE), java.math.BigDecimal.class);
    private static final Field<Boolean> ACTIVE_FIELD = DSL.field(DSL.name(Constants.Fields.IS_ACTIVE), Boolean.class);
    private static final Field<java.time.Instant> CREATED_AT_FIELD = DSL.field(DSL.name(Constants.Fields.CREATED_AT), java.time.Instant.class);
    private static final Field<java.time.Instant> UPDATED_AT_FIELD = DSL.field(DSL.name(Constants.Fields.UPDATED_AT), java.time.Instant.class);

    private final DSLContext dsl;
    private final PersistenceToDomainMapper mapper;

    public ExchangeInstrumentRepository(DSLContext dsl, PersistenceToDomainMapper mapper) {
        this.dsl = dsl;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Optional<ExchangeInstrument> findById(UUID id) {
        Record record = dsl.select(ID_FIELD, EXCHANGE_ID_FIELD, INSTRUMENT_ID_FIELD, EXCHANGE_SYMBOL_FIELD,
                    MIN_TRADE_QUANTITY_FIELD, TICK_SIZE_FIELD, MAKER_FEE_FIELD, TAKER_FEE_FIELD, ACTIVE_FIELD,
                    CREATED_AT_FIELD, UPDATED_AT_FIELD)
            .from(EXCHANGE_INSTRUMENT_TABLE)
            .where(ID_FIELD.eq(id))
            .fetchOne();
        if (record == null) {
            return Optional.empty();
        }
        ExchangeInstrumentRecordModel model = new ExchangeInstrumentRecordModel();
        model.setId(record.get(ID_FIELD));
        model.setExchangeId(record.get(EXCHANGE_ID_FIELD));
        model.setInstrumentId(record.get(INSTRUMENT_ID_FIELD));
        model.setExchangeSymbol(record.get(EXCHANGE_SYMBOL_FIELD));
        model.setMinTradeQuantity(record.get(MIN_TRADE_QUANTITY_FIELD));
        model.setTickSize(record.get(TICK_SIZE_FIELD));
        model.setMakerFeeRate(record.get(MAKER_FEE_FIELD));
        model.setTakerFeeRate(record.get(TAKER_FEE_FIELD));
        model.setActive(record.get(ACTIVE_FIELD));
        model.setCreatedAt(record.get(CREATED_AT_FIELD));
        model.setUpdatedAt(record.get(UPDATED_AT_FIELD));
        return Optional.of(mapper.toDomain(model));
    }
}
