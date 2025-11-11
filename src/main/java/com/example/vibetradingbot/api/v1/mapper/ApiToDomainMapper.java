package com.example.vibetradingbot.api.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.vibetradingbot.api.v1.dto.OnboardingRequest;
import com.example.vibetradingbot.domain.model.OnboardingCommand;

/**
 * Маппер API DTO в доменные команды.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ApiToDomainMapper {

    @Mapping(target = "timeframe", expression = "java(com.example.vibetradingbot.domain.enums.CanonicalTimeframe.fromCode(request.getTimeframe()))")
    OnboardingCommand toCommand(OnboardingRequest request);
}
