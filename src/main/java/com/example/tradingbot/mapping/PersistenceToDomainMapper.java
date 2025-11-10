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
import java.time.Instant;
import java.util.Objects;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface PersistenceToDomainMapper {

    default Instrument toDomain(InstrumentEntity entity) {
        if (entity == null) {
            return null;
        }
        return Instrument.rehydrate(
            entity.getId(),
            entity.getName(),
            entity.getBase(),
            entity.getQuote(),
            entity.getPriceStep(),
            entity.getQtyStep(),
            entity.getMinNotional(),
            Boolean.TRUE.equals(entity.getIsPerpetual())
        );
    }

    default Exchange toDomain(ExchangeEntity entity) {
        if (entity == null) {
            return null;
        }
        return Exchange.rehydrate(entity.getId(), entity.getName());
    }

    default ExchangeInstrument toDomain(
        ExchangeInstrumentEntity entity,
        Exchange exchange,
        Instrument instrument
    ) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(exchange, "exchange");
        Objects.requireNonNull(instrument, "instrument");
        return ExchangeInstrument.rehydrate(
            entity.getId(),
            exchange,
            instrument,
            entity.getClientSymbol(),
            entity.getContractType(),
            entity.getMarginMode(),
            entity.getLeverageMax()
        );
    }

    default HistoryGroup toDomain(
        HistoryGroupEntity entity,
        ExchangeInstrument exchangeInstrument,
        java.util.Set<String> allowedTimeframes
    ) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(exchangeInstrument, "exchangeInstrument");
        return HistoryGroup.rehydrate(
            entity.getId(),
            exchangeInstrument,
            com.example.tradingbot.domain.value.Timeframe.of(entity.getTimeframeCanonical(), allowedTimeframes),
            entity.getCoverageStartUtc() != null ? com.example.tradingbot.domain.value.TimestampUtc.of(entity.getCoverageStartUtc()) : null
        );
    }

    default IndicatorParams toDomain(IndicatorParamsEntity entity) {
        if (entity == null) {
            return null;
        }
        return IndicatorParams.rehydrate(
            entity.getId(),
            entity.getType(),
            entity.getTimeframeCanonical(),
            entity.getVersion(),
            entity.getCanonicalJson(),
            entity.getCreatedAt(),
            entity.getCreatedBy(),
            Boolean.TRUE.equals(entity.getIsActive())
        );
    }

    default SignalParams toSignalParams(SignalParamsEntity entity) {
        if (entity == null) {
            return null;
        }
        return SignalParams.rehydrate(
            entity.getId(),
            entity.getType(),
            entity.getTimeframeCanonical(),
            entity.getVersion(),
            entity.getCanonicalJson(),
            entity.getCreatedAt(),
            entity.getCreatedBy(),
            Boolean.TRUE.equals(entity.getIsActive())
        );
    }

    default QuorumParams toQuorumParams(QuorumParamsEntity entity) {
        if (entity == null) {
            return null;
        }
        return QuorumParams.rehydrate(
            entity.getId(),
            entity.getVersion(),
            entity.getCanonicalJson(),
            entity.getCreatedAt(),
            entity.getCreatedBy(),
            Boolean.TRUE.equals(entity.getIsActive())
        );
    }

    default RiskParams toRiskParams(RiskParamsEntity entity) {
        if (entity == null) {
            return null;
        }
        return RiskParams.rehydrate(
            entity.getId(),
            entity.getVersion(),
            entity.getCanonicalJson(),
            entity.getCreatedAt(),
            entity.getCreatedBy(),
            Boolean.TRUE.equals(entity.getIsActive())
        );
    }

    default ExchangeParams toExchangeParams(
        ExchangeParamsEntity entity,
        Exchange exchange
    ) {
        Objects.requireNonNull(exchange, "exchange");
        return ExchangeParams.rehydrate(
            entity.getId(),
            exchange,
            entity.getVersion(),
            entity.getCanonicalJson(),
            entity.getCreatedAt(),
            entity.getCreatedBy(),
            Boolean.TRUE.equals(entity.getIsActive())
        );
    }

    default IndicatorCheckpoint toCheckpoint(
        IndicatorCheckpointEntity entity,
        HistoryGroup historyGroup,
        IndicatorParams params
    ) {
        Objects.requireNonNull(historyGroup, "historyGroup");
        Objects.requireNonNull(params, "params");
        Instant lastTs = entity.getLastTs();
        return IndicatorCheckpoint.rehydrate(
            entity.getId(),
            entity.getIndicator(),
            historyGroup,
            params,
            entity.getVersion(),
            lastTs
        );
    }
}
