package com.example.vibetradingbot.api.v1.mapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.vibetradingbot.api.v1.dto.CanonicalTimeframeDto;
import com.example.vibetradingbot.domain.model.CanonicalTimeframeDefinition;

/**
 * Маппер доменных моделей в API DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DomainToApiMapper {

    @Mapping(target = "code", expression = "java(definition.getTimeframe().name())")
    @Mapping(source = "exchangeMappings", target = "exchanges")
    CanonicalTimeframeDto toDto(CanonicalTimeframeDefinition definition);

    default List<CanonicalTimeframeDto> toDtoList(Collection<CanonicalTimeframeDefinition> definitions) {
        return definitions.stream().map(this::toDto).collect(Collectors.toList());
    }
}
