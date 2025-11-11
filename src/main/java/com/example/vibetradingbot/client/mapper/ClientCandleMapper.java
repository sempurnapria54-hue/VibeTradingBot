package com.example.vibetradingbot.client.mapper;

import org.springframework.stereotype.Component;

import com.example.vibetradingbot.client.model.ClientCandle;
import com.example.vibetradingbot.client.validator.ClientCandleValidator;
import com.example.vibetradingbot.domain.model.Candle;

/**
 * Обёртка над MapStruct маппером с валидацией входящих данных.
 */
@Component
public class ClientCandleMapper {

    private final ClientCandleValidator validator;
    private final ClientToDomainMapper mapper;

    public ClientCandleMapper(ClientCandleValidator validator, ClientToDomainMapper mapper) {
        this.validator = validator;
        this.mapper = mapper;
    }

    public Candle map(ClientCandle clientCandle) {
        validator.validate(clientCandle);
        return mapper.toDomain(clientCandle);
    }
}
