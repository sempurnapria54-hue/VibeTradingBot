package com.example.vibetradingbot.client.validator;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.example.vibetradingbot.client.model.ClientCandle;
import com.example.vibetradingbot.exception.ServiceException;
import com.example.vibetradingbot.util.Constants;

/**
 * Валидация свечей, полученных от внешних источников.
 */
@Component
public class ClientCandleValidator {

    public void validate(ClientCandle candle) {
        if (Objects.isNull(candle)) {
            throw new ServiceException(Constants.Errors.INVALID_CLIENT_PAYLOAD, Constants.Validation.NULL_CLIENT_CANDLE);
        }
        if (Objects.isNull(candle.getExchangeInstrumentId())) {
            throw new ServiceException(Constants.Errors.INVALID_CLIENT_PAYLOAD, Constants.Validation.NULL_IDENTIFIER);
        }
        if (Objects.isNull(candle.getTimeframe())) {
            throw new ServiceException(Constants.Errors.INVALID_CLIENT_PAYLOAD, Constants.Validation.NULL_TIMEFRAME);
        }
        if (Objects.isNull(candle.getOpenTime()) || Objects.isNull(candle.getCloseTime())
                || Objects.isNull(candle.getOpenPrice()) || Objects.isNull(candle.getClosePrice())
                || Objects.isNull(candle.getHighPrice()) || Objects.isNull(candle.getLowPrice())
                || Objects.isNull(candle.getVolume())) {
            throw new ServiceException(Constants.Errors.INVALID_CLIENT_PAYLOAD, Constants.Validation.NULL_IDENTIFIER);
        }
        if (candle.getVolume().compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException(Constants.Errors.INVALID_CLIENT_PAYLOAD, Constants.Validation.NEGATIVE_VOLUME);
        }
        if (!candle.getCloseTime().isAfter(candle.getOpenTime())) {
            throw new ServiceException(Constants.Errors.INVALID_CLIENT_PAYLOAD, Constants.Validation.INVALID_TIME_RANGE);
        }
    }
}
