package com.example.tradingbot.mapping;

import com.example.tradingbot.client.common.ClientCandle;
import com.example.tradingbot.domain.model.Candle;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface DomainToClientMapper {

    default ClientCandle toClient(Candle candle) {
        return ClientCandle.builder()
            .timestamp(candle.timestampUtc().toInstant())
            .open(candle.open())
            .high(candle.high())
            .low(candle.low())
            .close(candle.close())
            .volumeBase(candle.volumeCoin() != null ? candle.volumeCoin().toBigDecimal() : null)
            .volumeQuote(candle.volumeCurrency() != null ? candle.volumeCurrency().toBigDecimal() : null)
            .build();
    }
}
