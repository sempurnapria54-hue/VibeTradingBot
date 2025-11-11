package com.example.vibetradingbot.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.example.vibetradingbot.config.ConsistencyProperties;
import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.model.Candle;
import com.example.vibetradingbot.domain.model.CandleGap;
import com.example.vibetradingbot.domain.model.ConsistencyScanResult;
import com.example.vibetradingbot.domain.model.HistoryOperationResult;
import com.example.vibetradingbot.persistence.repository.CandleRepository;
import com.example.vibetradingbot.util.Constants;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Сервис контроля целостности данных свечей.
 */
@Service
public class ConsistencyService {

    private final ConsistencyProperties properties;
    private final CandleRepository candleRepository;
    private final HistoryService historyService;
    private final MeterRegistry meterRegistry;

    public ConsistencyService(ConsistencyProperties properties, CandleRepository candleRepository,
            HistoryService historyService, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.candleRepository = candleRepository;
        this.historyService = historyService;
        this.meterRegistry = meterRegistry;
    }

    @Transactional(readOnly = true)
    public ConsistencyScanResult scan(UUID exchangeInstrumentId, CanonicalTimeframe timeframe, Integer windowBars) {
        int barsToCheck = windowBars == null || windowBars <= 0 ? properties.getScanWindowBars() : windowBars;
        Duration step = timeframe.getDuration();

        Optional<Candle> latestOptional = candleRepository.findLatestCandle(exchangeInstrumentId, timeframe);
        if (latestOptional.isEmpty()) {
            meterRegistry.counter(Constants.Metrics.CONSISTENCY_SCANS_TOTAL, createTags(exchangeInstrumentId, timeframe))
                .increment();
            return ConsistencyScanResult.create(exchangeInstrumentId, timeframe, Collections.emptyList(), 0, 0, null);
        }

        Instant latestOpen = latestOptional.get().getOpenTime();
        Instant start = latestOpen.minus(step.multipliedBy(barsToCheck));
        Instant end = latestOpen.plus(step);
        List<Candle> candles = candleRepository.findCandlesBetween(exchangeInstrumentId, timeframe, start, end);
        List<Instant> actualOpens = candles.stream().map(Candle::getOpenTime).sorted().collect(Collectors.toList());
        Set<Instant> actualSet = new HashSet<>(actualOpens);

        List<Instant> missing = new ArrayList<>();
        if (!actualOpens.isEmpty()) {
            Instant cursor = actualOpens.get(0);
            Instant last = actualOpens.get(actualOpens.size() - 1);
            while (!cursor.isAfter(last)) {
                if (!actualSet.contains(cursor)) {
                    missing.add(cursor);
                }
                cursor = cursor.plus(step);
            }
        }

        long misaligned = candles.stream().filter(candle -> !isAligned(candle.getOpenTime(), step)).count();
        List<CandleGap> gaps = buildGaps(missing, step);

        meterRegistry.counter(Constants.Metrics.CONSISTENCY_SCANS_TOTAL, createTags(exchangeInstrumentId, timeframe))
            .increment();
        meterRegistry.counter(Constants.Metrics.CONSISTENCY_GAPS_FOUND, createTags(exchangeInstrumentId, timeframe))
            .increment(missing.size());

        return ConsistencyScanResult.create(exchangeInstrumentId, timeframe, gaps, missing.size(), misaligned,
                latestOptional.get().getCloseTime());
    }

    @Transactional
    public List<HistoryOperationResult> gapFill(UUID exchangeInstrumentId, CanonicalTimeframe timeframe,
            List<CandleGap> gaps) {
        if (CollectionUtils.isEmpty(gaps)) {
            return Collections.emptyList();
        }
        List<HistoryOperationResult> results = new ArrayList<>();
        for (CandleGap gap : gaps) {
            results.add(historyService.refillRange(exchangeInstrumentId, timeframe, gap.getFromUtc(), gap.getToUtc()));
        }
        meterRegistry.counter(Constants.Metrics.CONSISTENCY_GAPS_FILLED, createTags(exchangeInstrumentId, timeframe))
            .increment(gaps.size());
        return results;
    }

    private boolean isAligned(Instant openTime, Duration step) {
        long seconds = step.getSeconds();
        if (seconds <= 0L) {
            return true;
        }
        long openSeconds = openTime.getEpochSecond();
        return openSeconds % seconds == 0;
    }

    private List<CandleGap> buildGaps(List<Instant> missing, Duration step) {
        if (missing.isEmpty()) {
            return Collections.emptyList();
        }
        List<CandleGap> gaps = new ArrayList<>();
        Instant currentStart = missing.get(0);
        Instant previous = currentStart;
        for (int index = 1; index < missing.size(); index++) {
            Instant instant = missing.get(index);
            if (!instant.equals(previous.plus(step))) {
                gaps.add(CandleGap.create(currentStart, previous.plus(step)));
                currentStart = instant;
            }
            previous = instant;
        }
        gaps.add(CandleGap.create(currentStart, previous.plus(step)));
        return gaps;
    }

    private String[] createTags(UUID exchangeInstrumentId, CanonicalTimeframe timeframe) {
        return new String[] {
            "exchangeInstrumentId", exchangeInstrumentId.toString(),
            "timeframe", timeframe.name()
        };
    }
}
