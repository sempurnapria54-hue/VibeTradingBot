package com.example.vibetradingbot.client.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.vibetradingbot.client.model.ClientCandle;
import com.example.vibetradingbot.domain.model.Candle;

/**
 * Маппер внешних моделей в доменные сущности.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ClientToDomainMapper {

    @Mapping(target = "id", source = "externalId")
    @Mapping(target = "timeframe", expression = "java(com.example.vibetradingbot.domain.enums.CanonicalTimeframe.fromCode(clientCandle.getTimeframe()))")
    @Mapping(target = "coverageEnd", source = "closeTime")
    Candle toDomain(ClientCandle clientCandle);
}
