package com.example.tradingbot.mapping;

import com.example.tradingbot.api.v1.dto.ExchangeSymbolDto;
import com.example.tradingbot.client.common.ExchangeSymbol;

import java.math.BigDecimal;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface DomainToApiMapper {

    default ExchangeSymbolDto toDto(ExchangeSymbol symbol) {
        BigDecimal leverage = symbol.getLeverageMax() != null ? symbol.getLeverageMax() : BigDecimal.ZERO;
        return new ExchangeSymbolDto(
            symbol.getClientSymbol(),
            symbol.getContractType(),
            symbol.getMarginMode(),
            leverage,
            symbol.getPriceStep(),
            symbol.getQuantityStep()
        );
    }
}
