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

import com.example.vibetradingbot.domain.model.Exchange;
import com.example.vibetradingbot.persistence.mapper.PersistenceToDomainMapper;
import com.example.vibetradingbot.persistence.model.ExchangeRecordModel;
import com.example.vibetradingbot.util.Constants;

/**
 * Репозиторий доступа к биржам.
 */
@Repository
public class ExchangeRepository {

    private static final Table<Record> EXCHANGE_TABLE = DSL.table(DSL.name(Constants.Schemas.EXCHANGE, Constants.Fields.EXCHANGE));

    private static final Field<UUID> ID_FIELD = DSL.field(DSL.name(Constants.Fields.ID), UUID.class);
    private static final Field<String> CODE_FIELD = DSL.field(DSL.name(Constants.Fields.CODE), String.class);
    private static final Field<String> NAME_FIELD = DSL.field(DSL.name(Constants.Fields.NAME), String.class);
    private static final Field<String> STATUS_FIELD = DSL.field(DSL.name(Constants.Fields.STATUS), String.class);
    private static final Field<java.time.Instant> CREATED_AT_FIELD = DSL.field(DSL.name(Constants.Fields.CREATED_AT), java.time.Instant.class);
    private static final Field<java.time.Instant> UPDATED_AT_FIELD = DSL.field(DSL.name(Constants.Fields.UPDATED_AT), java.time.Instant.class);

    private final DSLContext dsl;
    private final PersistenceToDomainMapper mapper;

    public ExchangeRepository(DSLContext dsl, PersistenceToDomainMapper mapper) {
        this.dsl = dsl;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Optional<Exchange> findById(UUID id) {
        Record record = dsl.select(ID_FIELD, CODE_FIELD, NAME_FIELD, STATUS_FIELD, CREATED_AT_FIELD, UPDATED_AT_FIELD)
            .from(EXCHANGE_TABLE)
            .where(ID_FIELD.eq(id))
            .fetchOne();
        if (record == null) {
            return Optional.empty();
        }
        ExchangeRecordModel model = new ExchangeRecordModel();
        model.setId(record.get(ID_FIELD));
        model.setCode(record.get(CODE_FIELD));
        model.setName(record.get(NAME_FIELD));
        model.setStatus(record.get(STATUS_FIELD));
        model.setCreatedAt(record.get(CREATED_AT_FIELD));
        model.setUpdatedAt(record.get(UPDATED_AT_FIELD));
        return Optional.of(mapper.toDomain(model));
    }
}
