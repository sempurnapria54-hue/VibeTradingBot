package com.example.vibetradingbot.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.vibetradingbot.domain.model.Candle;
import com.example.vibetradingbot.domain.model.CandleCoverage;
import com.example.vibetradingbot.domain.model.Exchange;
import com.example.vibetradingbot.domain.model.ExchangeInstrument;
import com.example.vibetradingbot.persistence.model.CandleCoverageRecordModel;
import com.example.vibetradingbot.persistence.model.CandleRecordModel;
import com.example.vibetradingbot.persistence.model.ExchangeInstrumentRecordModel;
import com.example.vibetradingbot.persistence.model.ExchangeRecordModel;

/**
 * Маппер persistence моделей в доменные сущности.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PersistenceToDomainMapper {

    @Mapping(target = "timeframe", expression = "java(com.example.vibetradingbot.domain.enums.CanonicalTimeframe.fromCode(record.getTimeframe()))")
    @Mapping(target = "openTime", source = "openTimeUtc")
    @Mapping(target = "closeTime", source = "closeTimeUtc")
    @Mapping(target = "coverageEnd", source = "coverageEndUtc")
    Candle toDomain(CandleRecordModel record);

    @Mapping(target = "timeframe", expression = "java(com.example.vibetradingbot.domain.enums.CanonicalTimeframe.fromCode(record.getTimeframe()))")
    @Mapping(target = "coverageStart", source = "coverageStartUtc")
    @Mapping(target = "coverageEnd", source = "coverageEndUtc")
    @Mapping(target = "complete", source = "complete")
    CandleCoverage toCoverage(CandleCoverageRecordModel record);

    @Mapping(target = "status", expression = "java(com.example.vibetradingbot.domain.enums.ExchangeStatus.valueOf(record.getStatus()))")
    Exchange toDomain(ExchangeRecordModel record);

    @Mapping(target = "active", expression = "java(Boolean.TRUE.equals(record.getActive()))")
    ExchangeInstrument toDomain(ExchangeInstrumentRecordModel record);
}
