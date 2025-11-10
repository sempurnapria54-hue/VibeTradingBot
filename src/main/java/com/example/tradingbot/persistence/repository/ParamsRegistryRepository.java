package com.example.tradingbot.persistence.repository;

import com.example.tradingbot.domain.model.params.ExchangeParams;
import com.example.tradingbot.domain.model.params.IndicatorParams;
import com.example.tradingbot.domain.model.params.QuorumParams;
import com.example.tradingbot.domain.model.params.RiskParams;
import com.example.tradingbot.domain.model.params.SignalParams;
import com.example.tradingbot.mapping.PersistenceToDomainMapper;
import com.example.tradingbot.persistence.model.ExchangeParamsEntity;
import com.example.tradingbot.persistence.model.IndicatorParamsEntity;
import com.example.tradingbot.persistence.model.QuorumParamsEntity;
import com.example.tradingbot.persistence.model.RiskParamsEntity;
import com.example.tradingbot.persistence.model.SignalParamsEntity;

import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class ParamsRegistryRepository {

    private static final Table<Record> INDICATOR_TABLE = DSL.table("indicator_params");
    private static final Field<Long> INDICATOR_ID = DSL.field("id", Long.class);
    private static final Field<String> INDICATOR_TYPE = DSL.field("type", String.class);
    private static final Field<String> INDICATOR_TF = DSL.field("timeframe_canonical", String.class);
    private static final Field<String> INDICATOR_VERSION = DSL.field("version", String.class);
    private static final Field<String> INDICATOR_JSON = DSL.field("canonical_json", String.class);

    private static final Table<Record> SIGNAL_TABLE = DSL.table("signal_params");
    private static final Field<Long> SIGNAL_ID = DSL.field("id", Long.class);
    private static final Field<String> SIGNAL_TYPE = DSL.field("type", String.class);
    private static final Field<String> SIGNAL_TF = DSL.field("timeframe_canonical", String.class);
    private static final Field<String> SIGNAL_VERSION = DSL.field("version", String.class);
    private static final Field<String> SIGNAL_JSON = DSL.field("canonical_json", String.class);

    private static final Table<Record> QUORUM_TABLE = DSL.table("quorum_params");
    private static final Field<Long> QUORUM_ID = DSL.field("id", Long.class);
    private static final Field<String> QUORUM_VERSION = DSL.field("version", String.class);
    private static final Field<String> QUORUM_JSON = DSL.field("canonical_json", String.class);

    private static final Table<Record> RISK_TABLE = DSL.table("risk_params");
    private static final Field<Long> RISK_ID = DSL.field("id", Long.class);
    private static final Field<String> RISK_VERSION = DSL.field("version", String.class);
    private static final Field<String> RISK_JSON = DSL.field("canonical_json", String.class);

    private static final Table<Record> EXCHANGE_TABLE = DSL.table("exchange_params");
    private static final Field<Long> EXCHANGE_ID = DSL.field("id", Long.class);
    private static final Field<Long> EXCHANGE_EXCHANGE_ID = DSL.field("exchange_id", Long.class);
    private static final Field<String> EXCHANGE_VERSION = DSL.field("version", String.class);
    private static final Field<String> EXCHANGE_JSON = DSL.field("canonical_json", String.class);

    private final DSLContext dsl;
    private final PersistenceToDomainMapper mapper;

    public ParamsRegistryRepository(DSLContext dsl, PersistenceToDomainMapper mapper) {
        this.dsl = dsl;
        this.mapper = mapper;
    }

    public IndicatorParams resolveIndicator(IndicatorParams params) {
        Optional<IndicatorParams> existing = dsl.selectFrom(INDICATOR_TABLE)
            .where(INDICATOR_TYPE.eq(params.type())
                .and(INDICATOR_TF.eq(params.timeframeCanonical()))
                .and(INDICATOR_VERSION.eq(params.version()))
                .and(INDICATOR_JSON.eq(params.canonicalJson())))
            .fetchOptional(record -> mapper.toDomain(toIndicatorEntity(record)));
        if (existing.isPresent()) {
            return existing.get();
        }
        Long id = dsl.insertInto(INDICATOR_TABLE)
            .set(INDICATOR_TYPE, params.type())
            .set(INDICATOR_TF, params.timeframeCanonical())
            .set(INDICATOR_VERSION, params.version())
            .set(INDICATOR_JSON, params.canonicalJson())
            .returningResult(INDICATOR_ID)
            .fetchOne(INDICATOR_ID);
        if (id == null) {
            return resolveIndicator(params);
        }
        return params.withId(id);
    }

    public SignalParams resolveSignal(SignalParams params) {
        Optional<SignalParams> existing = dsl.selectFrom(SIGNAL_TABLE)
            .where(SIGNAL_TYPE.eq(params.type())
                .and(SIGNAL_TF.eq(params.timeframeCanonical()))
                .and(SIGNAL_VERSION.eq(params.version()))
                .and(SIGNAL_JSON.eq(params.canonicalJson())))
            .fetchOptional(record -> mapper.toSignalParams(toSignalEntity(record)));
        if (existing.isPresent()) {
            return existing.get();
        }
        Long id = dsl.insertInto(SIGNAL_TABLE)
            .set(SIGNAL_TYPE, params.type())
            .set(SIGNAL_TF, params.timeframeCanonical())
            .set(SIGNAL_VERSION, params.version())
            .set(SIGNAL_JSON, params.canonicalJson())
            .returningResult(SIGNAL_ID)
            .fetchOne(SIGNAL_ID);
        if (id == null) {
            return resolveSignal(params);
        }
        return params.withId(id);
    }

    public QuorumParams resolveQuorum(QuorumParams params) {
        Optional<QuorumParams> existing = dsl.selectFrom(QUORUM_TABLE)
            .where(QUORUM_VERSION.eq(params.version()).and(QUORUM_JSON.eq(params.canonicalJson())))
            .fetchOptional(record -> mapper.toQuorumParams(toQuorumEntity(record)));
        if (existing.isPresent()) {
            return existing.get();
        }
        Long id = dsl.insertInto(QUORUM_TABLE)
            .set(QUORUM_VERSION, params.version())
            .set(QUORUM_JSON, params.canonicalJson())
            .returningResult(QUORUM_ID)
            .fetchOne(QUORUM_ID);
        if (id == null) {
            return resolveQuorum(params);
        }
        return params.withId(id);
    }

    public RiskParams resolveRisk(RiskParams params) {
        Optional<RiskParams> existing = dsl.selectFrom(RISK_TABLE)
            .where(RISK_VERSION.eq(params.version()).and(RISK_JSON.eq(params.canonicalJson())))
            .fetchOptional(record -> mapper.toRiskParams(toRiskEntity(record)));
        if (existing.isPresent()) {
            return existing.get();
        }
        Long id = dsl.insertInto(RISK_TABLE)
            .set(RISK_VERSION, params.version())
            .set(RISK_JSON, params.canonicalJson())
            .returningResult(RISK_ID)
            .fetchOne(RISK_ID);
        if (id == null) {
            return resolveRisk(params);
        }
        return params.withId(id);
    }

    public ExchangeParams resolveExchange(ExchangeParams params) {
        Optional<ExchangeParams> existing = dsl.selectFrom(EXCHANGE_TABLE)
            .where(EXCHANGE_EXCHANGE_ID.eq(params.exchange().id())
                .and(EXCHANGE_VERSION.eq(params.version()))
                .and(EXCHANGE_JSON.eq(params.canonicalJson())))
            .fetchOptional(record -> mapper.toExchangeParams(toExchangeEntity(record), params.exchange()));
        if (existing.isPresent()) {
            return existing.get();
        }
        Long id = dsl.insertInto(EXCHANGE_TABLE)
            .set(EXCHANGE_EXCHANGE_ID, params.exchange().id())
            .set(EXCHANGE_VERSION, params.version())
            .set(EXCHANGE_JSON, params.canonicalJson())
            .returningResult(EXCHANGE_ID)
            .fetchOne(EXCHANGE_ID);
        if (id == null) {
            return resolveExchange(params);
        }
        return params.withId(id);
    }

    private IndicatorParamsEntity toIndicatorEntity(Record record) {
        IndicatorParamsEntity entity = new IndicatorParamsEntity();
        entity.setId(record.get(INDICATOR_ID));
        entity.setType(record.get(INDICATOR_TYPE));
        entity.setTimeframeCanonical(record.get(INDICATOR_TF));
        entity.setVersion(record.get(INDICATOR_VERSION));
        entity.setCanonicalJson(record.get(INDICATOR_JSON));
        return entity;
    }

    private SignalParamsEntity toSignalEntity(Record record) {
        SignalParamsEntity entity = new SignalParamsEntity();
        entity.setId(record.get(SIGNAL_ID));
        entity.setType(record.get(SIGNAL_TYPE));
        entity.setTimeframeCanonical(record.get(SIGNAL_TF));
        entity.setVersion(record.get(SIGNAL_VERSION));
        entity.setCanonicalJson(record.get(SIGNAL_JSON));
        return entity;
    }

    private QuorumParamsEntity toQuorumEntity(Record record) {
        QuorumParamsEntity entity = new QuorumParamsEntity();
        entity.setId(record.get(QUORUM_ID));
        entity.setVersion(record.get(QUORUM_VERSION));
        entity.setCanonicalJson(record.get(QUORUM_JSON));
        return entity;
    }

    private RiskParamsEntity toRiskEntity(Record record) {
        RiskParamsEntity entity = new RiskParamsEntity();
        entity.setId(record.get(RISK_ID));
        entity.setVersion(record.get(RISK_VERSION));
        entity.setCanonicalJson(record.get(RISK_JSON));
        return entity;
    }

    private ExchangeParamsEntity toExchangeEntity(Record record) {
        ExchangeParamsEntity entity = new ExchangeParamsEntity();
        entity.setId(record.get(EXCHANGE_ID));
        entity.setExchangeId(record.get(EXCHANGE_EXCHANGE_ID));
        entity.setVersion(record.get(EXCHANGE_VERSION));
        entity.setCanonicalJson(record.get(EXCHANGE_JSON));
        return entity;
    }
}
