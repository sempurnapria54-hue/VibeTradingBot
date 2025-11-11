package com.example.vibetradingbot.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.vibetradingbot.domain.model.Candle;
import com.example.vibetradingbot.domain.model.CandleCoverage;
import com.example.vibetradingbot.persistence.model.CandleCoverageRecordModel;
import com.example.vibetradingbot.persistence.model.CandleRecordModel;

/**
 * Маппер доменных моделей в persistence представление.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DomainToPersistenceMapper {

    @Mapping(target = "timeframe", expression = "java(candle.getTimeframe().name())")
    @Mapping(target = "openTimeUtc", source = "openTime")
    @Mapping(target = "closeTimeUtc", source = "closeTime")
    @Mapping(target = "coverageEndUtc", source = "coverageEnd")
    CandleRecordModel toRecord(Candle candle);

    @Mapping(target = "timeframe", expression = "java(coverage.getTimeframe().name())")
    @Mapping(target = "coverageStartUtc", source = "coverageStart")
    @Mapping(target = "coverageEndUtc", source = "coverageEnd")
    CandleCoverageRecordModel toRecord(CandleCoverage coverage);
}
