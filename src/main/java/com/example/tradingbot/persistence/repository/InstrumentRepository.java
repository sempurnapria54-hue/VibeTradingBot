package com.example.tradingbot.persistence.repository;

import com.example.tradingbot.domain.model.Instrument;
import com.example.tradingbot.mapping.PersistenceToDomainMapper;
import com.example.tradingbot.persistence.model.InstrumentEntity;

import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class InstrumentRepository {

    private static final Table<Record> TABLE = DSL.table("instrument");
    private static final Field<Long> ID = DSL.field("id", Long.class);
    private static final Field<String> NAME = DSL.field("name", String.class);
    private static final Field<String> BASE = DSL.field("base", String.class);
    private static final Field<String> QUOTE = DSL.field("quote", String.class);
    private static final Field<java.math.BigDecimal> PRICE_STEP = DSL.field("price_step", java.math.BigDecimal.class);
    private static final Field<java.math.BigDecimal> QTY_STEP = DSL.field("qty_step", java.math.BigDecimal.class);
    private static final Field<java.math.BigDecimal> MIN_NOTIONAL = DSL.field("min_notional", java.math.BigDecimal.class);
    private static final Field<Boolean> IS_PERPETUAL = DSL.field("is_perpetual", Boolean.class);

    private final DSLContext dsl;
    private final PersistenceToDomainMapper mapper;

    public InstrumentRepository(DSLContext dsl, PersistenceToDomainMapper mapper) {
        this.dsl = dsl;
        this.mapper = mapper;
    }

    public Optional<Instrument> findById(long id) {
        return dsl.selectFrom(TABLE)
            .where(ID.eq(id))
            .fetchOptional(this::mapInstrument);
    }

    public Optional<Instrument> findByName(String name) {
        return dsl.selectFrom(TABLE)
            .where(NAME.eq(name))
            .fetchOptional(this::mapInstrument);
    }

    public Instrument saveIfAbsent(Instrument instrument) {
        Optional<Instrument> existing = findByName(instrument.name());
        if (existing.isPresent()) {
            return existing.get();
        }
        Long id = dsl.insertInto(TABLE)
            .set(NAME, instrument.name())
            .set(BASE, instrument.base())
            .set(QUOTE, instrument.quote())
            .set(PRICE_STEP, instrument.priceStep())
            .set(QTY_STEP, instrument.qtyStep())
            .set(MIN_NOTIONAL, instrument.minNotional())
            .set(IS_PERPETUAL, instrument.perpetual())
            .returningResult(ID)
            .fetchOne(ID);
        if (id == null) {
            return findByName(instrument.name()).orElseThrow();
        }
        return instrument.withId(id);
    }

    public List<Instrument> listAll() {
        return dsl.selectFrom(TABLE)
            .fetch(this::mapInstrument);
    }

    private Instrument mapInstrument(Record record) {
        InstrumentEntity entity = new InstrumentEntity();
        entity.setId(record.get(ID));
        entity.setName(record.get(NAME));
        entity.setBase(record.get(BASE));
        entity.setQuote(record.get(QUOTE));
        entity.setPriceStep(record.get(PRICE_STEP));
        entity.setQtyStep(record.get(QTY_STEP));
        entity.setMinNotional(record.get(MIN_NOTIONAL));
        entity.setIsPerpetual(record.get(IS_PERPETUAL));
        return mapper.toDomain(entity);
    }
}
