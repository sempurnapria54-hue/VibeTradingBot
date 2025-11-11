package com.example.vibetradingbot.api.v1.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.vibetradingbot.api.v1.dto.LiveEventDto;
import com.example.vibetradingbot.api.v1.dto.LiveOrderDto;
import com.example.vibetradingbot.api.v1.dto.LivePositionDto;
import com.example.vibetradingbot.api.v1.dto.RiskHaltStateDto;
import com.example.vibetradingbot.domain.model.OrderEvent;
import com.example.vibetradingbot.domain.model.OrderSnapshot;
import com.example.vibetradingbot.domain.model.PositionSnapshot;
import com.example.vibetradingbot.domain.model.RiskHaltState;

/**
 * MapStruct-маппер для live-торговли.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LiveMapper {

    LiveOrderDto toDto(OrderSnapshot snapshot);

    List<LiveOrderDto> toOrderDtos(List<OrderSnapshot> snapshots);

    LivePositionDto toDto(PositionSnapshot snapshot);

    List<LivePositionDto> toPositionDtos(List<PositionSnapshot> snapshots);

    LiveEventDto toDto(OrderEvent event);

    List<LiveEventDto> toEventDtos(List<OrderEvent> events);

    @Mapping(target = "halted", source = "halted")
    RiskHaltStateDto toDto(RiskHaltState state);

    List<RiskHaltStateDto> toHaltDtos(List<RiskHaltState> states);
}
