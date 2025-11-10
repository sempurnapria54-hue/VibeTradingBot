package com.example.tradingbot.mapping;

import com.example.tradingbot.domain.model.Exchange;
import com.example.tradingbot.domain.model.ExchangeInstrument;
import com.example.tradingbot.domain.model.HistoryGroup;
import com.example.tradingbot.domain.model.Instrument;
import com.example.tradingbot.domain.model.params.ExchangeParams;
import com.example.tradingbot.domain.model.params.IndicatorParams;
import com.example.tradingbot.domain.model.params.QuorumParams;
import com.example.tradingbot.domain.model.params.RiskParams;
import com.example.tradingbot.domain.model.params.SignalParams;
import com.example.tradingbot.domain.model.tech.IndicatorCheckpoint;
import com.example.tradingbot.persistence.model.ExchangeEntity;
import com.example.tradingbot.persistence.model.ExchangeInstrumentEntity;
import com.example.tradingbot.persistence.model.ExchangeParamsEntity;
import com.example.tradingbot.persistence.model.HistoryGroupEntity;
import com.example.tradingbot.persistence.model.IndicatorCheckpointEntity;
import com.example.tradingbot.persistence.model.IndicatorParamsEntity;
import com.example.tradingbot.persistence.model.InstrumentEntity;
import com.example.tradingbot.persistence.model.QuorumParamsEntity;
import com.example.tradingbot.persistence.model.RiskParamsEntity;
import com.example.tradingbot.persistence.model.SignalParamsEntity;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface DomainToPersistenceMapper {

    default InstrumentEntity toEntity(Instrument instrument) {
        if (instrument == null) {
            return null;
        }
        InstrumentEntity entity = new InstrumentEntity();
        entity.setId(instrument.id());
        entity.setName(instrument.name());
        entity.setBase(instrument.base());
        entity.setQuote(instrument.quote());
        entity.setPriceStep(instrument.priceStep());
        entity.setQtyStep(instrument.qtyStep());
        entity.setMinNotional(instrument.minNotional());
        entity.setIsPerpetual(instrument.perpetual());
        return entity;
    }

    default ExchangeEntity toEntity(Exchange exchange) {
        if (exchange == null) {
            return null;
        }
        ExchangeEntity entity = new ExchangeEntity();
        entity.setId(exchange.id());
        entity.setName(exchange.name());
        return entity;
    }

    default ExchangeInstrumentEntity toEntity(ExchangeInstrument exchangeInstrument) {
        if (exchangeInstrument == null) {
            return null;
        }
        ExchangeInstrumentEntity entity = new ExchangeInstrumentEntity();
        entity.setId(exchangeInstrument.id());
        entity.setExchangeId(exchangeInstrument.exchange().id());
        entity.setInstrumentId(exchangeInstrument.instrument().id());
        entity.setClientSymbol(exchangeInstrument.clientSymbol());
        entity.setContractType(exchangeInstrument.contractType());
        entity.setMarginMode(exchangeInstrument.marginMode());
        entity.setLeverageMax(exchangeInstrument.leverageMax());
        return entity;
    }

    default HistoryGroupEntity toEntity(HistoryGroup historyGroup) {
        if (historyGroup == null) {
            return null;
        }
        HistoryGroupEntity entity = new HistoryGroupEntity();
        entity.setId(historyGroup.id());
        entity.setExchangeInstrumentId(historyGroup.exchangeInstrument().id());
        entity.setTimeframeCanonical(historyGroup.timeframe().canonical());
        if (historyGroup.coverageStartUtc() != null) {
            entity.setCoverageStartUtc(historyGroup.coverageStartUtc().toInstant());
        }
        return entity;
    }

    default IndicatorParamsEntity toEntity(IndicatorParams params) {
        if (params == null) {
            return null;
        }
        IndicatorParamsEntity entity = new IndicatorParamsEntity();
        entity.setId(params.id());
        entity.setType(params.type());
        entity.setTimeframeCanonical(params.timeframeCanonical());
        entity.setVersion(params.version());
        entity.setCanonicalJson(params.canonicalJson());
        entity.setCreatedBy(params.createdBy());
        entity.setIsActive(params.active());
        if (params.createdAt() != null) {
            entity.setCreatedAt(params.createdAt());
        }
        return entity;
    }

    default SignalParamsEntity toEntity(SignalParams params) {
        if (params == null) {
            return null;
        }
        SignalParamsEntity entity = new SignalParamsEntity();
        entity.setId(params.id());
        entity.setType(params.type());
        entity.setTimeframeCanonical(params.timeframeCanonical());
        entity.setVersion(params.version());
        entity.setCanonicalJson(params.canonicalJson());
        entity.setCreatedBy(params.createdBy());
        entity.setIsActive(params.active());
        if (params.createdAt() != null) {
            entity.setCreatedAt(params.createdAt());
        }
        return entity;
    }

    default QuorumParamsEntity toEntity(QuorumParams params) {
        if (params == null) {
            return null;
        }
        QuorumParamsEntity entity = new QuorumParamsEntity();
        entity.setId(params.id());
        entity.setVersion(params.version());
        entity.setCanonicalJson(params.canonicalJson());
        entity.setCreatedBy(params.createdBy());
        entity.setIsActive(params.active());
        if (params.createdAt() != null) {
            entity.setCreatedAt(params.createdAt());
        }
        return entity;
    }

    default RiskParamsEntity toEntity(RiskParams params) {
        if (params == null) {
            return null;
        }
        RiskParamsEntity entity = new RiskParamsEntity();
        entity.setId(params.id());
        entity.setVersion(params.version());
        entity.setCanonicalJson(params.canonicalJson());
        entity.setCreatedBy(params.createdBy());
        entity.setIsActive(params.active());
        if (params.createdAt() != null) {
            entity.setCreatedAt(params.createdAt());
        }
        return entity;
    }

    default ExchangeParamsEntity toEntity(ExchangeParams params) {
        if (params == null) {
            return null;
        }
        ExchangeParamsEntity entity = new ExchangeParamsEntity();
        entity.setId(params.id());
        entity.setExchangeId(params.exchange().id());
        entity.setVersion(params.version());
        entity.setCanonicalJson(params.canonicalJson());
        entity.setCreatedBy(params.createdBy());
        entity.setIsActive(params.active());
        if (params.createdAt() != null) {
            entity.setCreatedAt(params.createdAt());
        }
        return entity;
    }

    default IndicatorCheckpointEntity toEntity(IndicatorCheckpoint checkpoint) {
        if (checkpoint == null) {
            return null;
        }
        IndicatorCheckpointEntity entity = new IndicatorCheckpointEntity();
        entity.setId(checkpoint.id());
        entity.setIndicator(checkpoint.indicator());
        entity.setHistoryGroupId(checkpoint.historyGroup().id());
        entity.setIndicatorParamsId(checkpoint.params().id());
        entity.setVersion(checkpoint.version());
        entity.setLastTs(checkpoint.lastTimestamp().toInstant());
        return entity;
    }
}
