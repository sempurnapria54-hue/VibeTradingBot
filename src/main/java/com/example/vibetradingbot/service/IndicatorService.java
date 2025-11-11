package com.example.vibetradingbot.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.enums.IndicatorType;
import com.example.vibetradingbot.domain.model.IndicatorCheckpoint;
import com.example.vibetradingbot.domain.model.IndicatorOperationResult;

/**
 * Сервис расчёта значений индикаторов. Пока реализует только каркас без вычислений.
 */
@Service
public class IndicatorService {

    public IndicatorOperationResult fillEma(UUID exchangeInstrumentId, CanonicalTimeframe timeframe, long paramsRef) {
        return IndicatorOperationResult.builder()
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .indicator(IndicatorType.EMA)
            .paramsRef(paramsRef)
            .insertedValues(0)
            .warmupSkipped(0)
            .build();
    }

    public IndicatorOperationResult fillMacd(UUID exchangeInstrumentId, CanonicalTimeframe timeframe, long paramsRef) {
        return IndicatorOperationResult.builder()
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .indicator(IndicatorType.MACD)
            .paramsRef(paramsRef)
            .insertedValues(0)
            .warmupSkipped(0)
            .build();
    }

    public IndicatorOperationResult fillRsi(UUID exchangeInstrumentId, CanonicalTimeframe timeframe, long paramsRef) {
        return IndicatorOperationResult.builder()
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .indicator(IndicatorType.RSI)
            .paramsRef(paramsRef)
            .insertedValues(0)
            .warmupSkipped(0)
            .build();
    }

    public IndicatorOperationResult fillStoch(UUID exchangeInstrumentId, CanonicalTimeframe timeframe, long paramsRef) {
        return IndicatorOperationResult.builder()
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .indicator(IndicatorType.STOCH)
            .paramsRef(paramsRef)
            .insertedValues(0)
            .warmupSkipped(0)
            .build();
    }

    public IndicatorOperationResult fillBb(UUID exchangeInstrumentId, CanonicalTimeframe timeframe, long paramsRef) {
        return IndicatorOperationResult.builder()
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .indicator(IndicatorType.BB)
            .paramsRef(paramsRef)
            .insertedValues(0)
            .warmupSkipped(0)
            .build();
    }

    public IndicatorOperationResult fillObv(UUID exchangeInstrumentId, CanonicalTimeframe timeframe, long paramsRef) {
        return IndicatorOperationResult.builder()
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .indicator(IndicatorType.OBV)
            .paramsRef(paramsRef)
            .insertedValues(0)
            .warmupSkipped(0)
            .build();
    }

    public Optional<IndicatorCheckpoint> findCheckpoint(IndicatorType indicator, CanonicalTimeframe timeframe,
            UUID exchangeInstrumentId, long paramsRef) {
        return Optional.empty();
    }
}
