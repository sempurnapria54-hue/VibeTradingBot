package com.example.vibetradingbot.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;

import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.exception.ServiceException;
import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Доменная модель нормализованной свечи.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class Candle {

    private UUID id;
    private UUID exchangeInstrumentId;
    private CanonicalTimeframe timeframe;
    private Instant openTime;
    private Instant closeTime;
    private Instant coverageEnd;
    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal volume;
    private long tradesCount;

    @Builder
    private Candle(UUID id, UUID exchangeInstrumentId, CanonicalTimeframe timeframe, Instant openTime, Instant closeTime,
            Instant coverageEnd, BigDecimal openPrice, BigDecimal closePrice, BigDecimal highPrice, BigDecimal lowPrice,
            BigDecimal volume, long tradesCount) {
        this.id = Objects.requireNonNull(id, Constants.Validation.NULL_IDENTIFIER);
        this.exchangeInstrumentId = Objects.requireNonNull(exchangeInstrumentId, Constants.Validation.NULL_IDENTIFIER);
        this.timeframe = Objects.requireNonNull(timeframe, Constants.Validation.NULL_TIMEFRAME);
        this.openTime = Objects.requireNonNull(openTime, Constants.Validation.INVALID_TIME_RANGE);
        this.closeTime = Objects.requireNonNull(closeTime, Constants.Validation.INVALID_TIME_RANGE);
        this.coverageEnd = Objects.requireNonNull(coverageEnd, Constants.Validation.INVALID_TIME_RANGE);
        this.openPrice = Objects.requireNonNull(openPrice, Constants.Validation.NULL_IDENTIFIER);
        this.closePrice = Objects.requireNonNull(closePrice, Constants.Validation.NULL_IDENTIFIER);
        this.highPrice = Objects.requireNonNull(highPrice, Constants.Validation.NULL_IDENTIFIER);
        this.lowPrice = Objects.requireNonNull(lowPrice, Constants.Validation.NULL_IDENTIFIER);
        this.volume = Objects.requireNonNull(volume, Constants.Validation.NULL_IDENTIFIER);
        this.tradesCount = tradesCount;
        validateTimeRange();
        validatePrices();
    }

    public static Candle create(UUID id, UUID exchangeInstrumentId, CanonicalTimeframe timeframe, Instant openTime,
            Instant closeTime, Instant coverageEnd, BigDecimal openPrice, BigDecimal closePrice, BigDecimal highPrice,
            BigDecimal lowPrice, BigDecimal volume, long tradesCount) {
        return Candle.builder()
            .id(id)
            .exchangeInstrumentId(exchangeInstrumentId)
            .timeframe(timeframe)
            .openTime(openTime)
            .closeTime(closeTime)
            .coverageEnd(coverageEnd)
            .openPrice(openPrice)
            .closePrice(closePrice)
            .highPrice(highPrice)
            .lowPrice(lowPrice)
            .volume(volume)
            .tradesCount(tradesCount)
            .build();
    }

    public boolean isCoverageComplete() {
        return BooleanUtils.isFalse(Objects.isNull(coverageEnd)) && !coverageEnd.isBefore(closeTime);
    }

    private void validateTimeRange() {
        if (!closeTime.isAfter(openTime)) {
            throw new ServiceException(Constants.Errors.DOMAIN_VALIDATION_FAILED, Constants.Validation.INVALID_TIME_RANGE);
        }
    }

    private void validatePrices() {
        if (closePrice.compareTo(BigDecimal.ZERO) < 0 || openPrice.compareTo(BigDecimal.ZERO) < 0
                || highPrice.compareTo(BigDecimal.ZERO) < 0 || lowPrice.compareTo(BigDecimal.ZERO) < 0
                || volume.compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException(Constants.Errors.DOMAIN_VALIDATION_FAILED, Constants.Validation.NEGATIVE_VOLUME);
        }
    }
}
