package com.example.vibetradingbot.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.example.vibetradingbot.util.Constants;

import lombok.Getter;
import lombok.Setter;

/**
 * Конфигурация оффлайн-бэктеста.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = Constants.Config.BACKTEST_PREFIX)
public class BacktestProperties {

    private Integer latencyBars = 1;
    private BigDecimal slippageBps = BigDecimal.ZERO;
    private BigDecimal takerFeeBps = BigDecimal.ZERO;
    private BigDecimal fundingBpsPer8h = BigDecimal.ZERO;
    private Horizons horizons = new Horizons();
    private Trailing trailing = new Trailing();
    private Position position = new Position();

    @Getter
    @Setter
    public static class Horizons {

        private List<Integer> bars = new ArrayList<>();
        private List<BigDecimal> volAtr = new ArrayList<>();
        private TripleBarrier tripleBarrier = new TripleBarrier();
    }

    @Getter
    @Setter
    public static class TripleBarrier {

        private BigDecimal tpKatr = BigDecimal.ZERO;
        private BigDecimal slKatr = BigDecimal.ZERO;
        private Integer timeBars = 0;
    }

    @Getter
    @Setter
    public static class Trailing {

        private Boolean enabled = Boolean.FALSE;
        private Integer atrPeriod = 14;
        private BigDecimal trailKatr = BigDecimal.ZERO;
        private BigDecimal breakevenAtR = BigDecimal.ZERO;
    }

    @Getter
    @Setter
    public static class Position {

        private Integer leverageMax = 1;
        private Boolean isolated = Boolean.TRUE;
        private BigDecimal riskPerTradePct = BigDecimal.ZERO;
    }
}
