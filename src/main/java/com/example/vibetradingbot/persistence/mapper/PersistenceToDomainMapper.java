package com.example.vibetradingbot.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.vibetradingbot.domain.model.Candle;
import com.example.vibetradingbot.domain.model.CandleCoverage;
import com.example.vibetradingbot.persistence.model.CandleCoverageRecordModel;
import com.example.vibetradingbot.persistence.model.CandleRecordModel;

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
}
