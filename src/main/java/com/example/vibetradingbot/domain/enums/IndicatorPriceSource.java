package com.example.vibetradingbot.domain.enums;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.example.vibetradingbot.domain.model.Candle;

/**
 * Источник цены для расчёта индикатора.
 */
public enum IndicatorPriceSource {
    CLOSE {
        @Override
        public BigDecimal extractPrice(Candle candle, int scale, RoundingMode roundingMode) {
            return candle.getClosePrice().setScale(scale, roundingMode);
        }
    },
    HL2 {
        @Override
        public BigDecimal extractPrice(Candle candle, int scale, RoundingMode roundingMode) {
            BigDecimal high = candle.getHighPrice();
            BigDecimal low = candle.getLowPrice();
            return high.add(low)
                .divide(BigDecimal.valueOf(2L), scale, roundingMode);
        }
    },
    HLC3 {
        @Override
        public BigDecimal extractPrice(Candle candle, int scale, RoundingMode roundingMode) {
            BigDecimal high = candle.getHighPrice();
            BigDecimal low = candle.getLowPrice();
            BigDecimal close = candle.getClosePrice();
            return high.add(low).add(close)
                .divide(BigDecimal.valueOf(3L), scale, roundingMode);
        }
    },
    OHLC4 {
        @Override
        public BigDecimal extractPrice(Candle candle, int scale, RoundingMode roundingMode) {
            BigDecimal open = candle.getOpenPrice();
            BigDecimal high = candle.getHighPrice();
            BigDecimal low = candle.getLowPrice();
            BigDecimal close = candle.getClosePrice();
            return open.add(high).add(low).add(close)
                .divide(BigDecimal.valueOf(4L), scale, roundingMode);
        }
    };

    public abstract BigDecimal extractPrice(Candle candle, int scale, RoundingMode roundingMode);
}
