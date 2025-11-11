package com.example.vibetradingbot.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.example.vibetradingbot.client.mapper.ClientCandleMapper;
import com.example.vibetradingbot.client.model.ClientCandle;
import com.example.vibetradingbot.config.HistoryIngestionProperties;
import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.model.Candle;
import com.example.vibetradingbot.domain.model.CandleCoverage;
import com.example.vibetradingbot.domain.model.Exchange;
import com.example.vibetradingbot.domain.model.ExchangeInstrument;
import com.example.vibetradingbot.domain.model.HistoryOperationResult;
import com.example.vibetradingbot.exchange.config.ExchangeConnectorRegistry;
import com.example.vibetradingbot.exchange.connector.ExchangeMarketDataConnector;
import com.example.vibetradingbot.exchange.model.ExchangeVendor;
import com.example.vibetradingbot.exception.ServiceException;
import com.example.vibetradingbot.persistence.repository.CandleRepository;
import com.example.vibetradingbot.persistence.repository.ExchangeInstrumentRepository;
import com.example.vibetradingbot.persistence.repository.ExchangeRepository;
import com.example.vibetradingbot.util.Constants;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

/**
 * Сервис загрузки исторических свечей.
 */
@Service
public class HistoryService {

    private final HistoryIngestionProperties properties;
    private final ExchangeInstrumentRepository exchangeInstrumentRepository;
    private final ExchangeRepository exchangeRepository;
    private final ExchangeConnectorRegistry exchangeConnectorRegistry;
    private final CandleRepository candleRepository;
    private final ClientCandleMapper clientCandleMapper;
    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, AtomicLong> lagGauges = new ConcurrentHashMap<>();

    public HistoryService(HistoryIngestionProperties properties, ExchangeInstrumentRepository exchangeInstrumentRepository,
            ExchangeRepository exchangeRepository, ExchangeConnectorRegistry exchangeConnectorRegistry,
            CandleRepository candleRepository, ClientCandleMapper clientCandleMapper, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.exchangeInstrumentRepository = exchangeInstrumentRepository;
        this.exchangeRepository = exchangeRepository;
        this.exchangeConnectorRegistry = exchangeConnectorRegistry;
        this.candleRepository = candleRepository;
        this.clientCandleMapper = clientCandleMapper;
        this.meterRegistry = meterRegistry;
    }

    @Transactional(readOnly = true)
    public List<CandleCoverage> getCoverage(UUID exchangeInstrumentId) {
        return candleRepository.findAllCoverage(exchangeInstrumentId);
    }

    @Transactional
    public List<HistoryOperationResult> downloadAll(UUID exchangeInstrumentId, List<CanonicalTimeframe> timeframes) {
        List<CanonicalTimeframe> requestedTimeframes = resolveTimeframes(timeframes);
        return requestedTimeframes.stream()
            .sorted(Comparator.comparing(CanonicalTimeframe::getDuration).reversed())
            .map(timeframe -> downloadIncremental(exchangeInstrumentId, timeframe))
            .collect(Collectors.toList());
    }

    @Transactional
    public HistoryOperationResult downloadIncremental(UUID exchangeInstrumentId, CanonicalTimeframe timeframe) {
        ExchangeInstrument exchangeInstrument = loadExchangeInstrument(exchangeInstrumentId);
        Exchange exchange = loadExchange(exchangeInstrument.getExchangeId());
        ExchangeMarketDataConnector connector = loadConnector(exchange.getCode());

        meterRegistry.counter(Constants.Metrics.HISTORY_CALLS, createTags(exchange.getCode(), timeframe)).increment();

        Optional<Candle> latestCandle = candleRepository.findLatestCandle(exchangeInstrumentId, timeframe);
        Instant fromExclusive = latestCandle.map(Candle::getCloseTime).orElse(Instant.EPOCH);
        Instant toInclusive = Instant.now();

        List<ClientCandle> clientCandles = invokeConnector(connector, exchange.getCode(), exchangeInstrument, timeframe,
                fromExclusive, toInclusive);
        List<Candle> candles = mapCandles(clientCandles, toInclusive);
        int inserted = candleRepository.insertCandles(candles);
        HistoryOperationResult result = updateCoverageAndBuildResult(exchangeInstrumentId, timeframe, inserted);
        meterRegistry.counter(Constants.Metrics.HISTORY_CANDLES_INSERTED, createTags(exchange.getCode(), timeframe))
            .increment(inserted);
        updateLagGauge(exchangeInstrumentId, timeframe,
            candleRepository.findLatestCandle(exchangeInstrumentId, timeframe));
        return result;
    }

    @Transactional
    public HistoryOperationResult refillRange(UUID exchangeInstrumentId, CanonicalTimeframe timeframe, Instant fromUtc,
            Instant toUtc) {
        ExchangeInstrument exchangeInstrument = loadExchangeInstrument(exchangeInstrumentId);
        Exchange exchange = loadExchange(exchangeInstrument.getExchangeId());
        ExchangeMarketDataConnector connector = loadConnector(exchange.getCode());

        meterRegistry.counter(Constants.Metrics.HISTORY_CALLS, createTags(exchange.getCode(), timeframe)).increment();

        List<ClientCandle> clientCandles = invokeConnector(connector, exchange.getCode(), exchangeInstrument, timeframe,
                fromUtc, toUtc);
        List<Candle> candles = mapCandles(clientCandles, toUtc);
        int inserted = candleRepository.insertCandles(candles);
        HistoryOperationResult result = updateCoverageAndBuildResult(exchangeInstrumentId, timeframe, inserted);
        meterRegistry.counter(Constants.Metrics.HISTORY_CANDLES_INSERTED, createTags(exchange.getCode(), timeframe))
            .increment(inserted);
        updateLagGauge(exchangeInstrumentId, timeframe, candleRepository.findLatestCandle(exchangeInstrumentId, timeframe));
        return result;
    }

    private List<CanonicalTimeframe> resolveTimeframes(List<CanonicalTimeframe> timeframes) {
        if (CollectionUtils.isEmpty(timeframes)) {
            return List.of(CanonicalTimeframe.values());
        }
        return new ArrayList<>(timeframes);
    }

    private ExchangeInstrument loadExchangeInstrument(UUID exchangeInstrumentId) {
        return exchangeInstrumentRepository.findById(exchangeInstrumentId)
            .orElseThrow(() -> new ServiceException(Constants.Errors.RESOURCE_NOT_FOUND, exchangeInstrumentId.toString()));
    }

    private Exchange loadExchange(UUID exchangeId) {
        return exchangeRepository.findById(exchangeId)
            .orElseThrow(() -> new ServiceException(Constants.Errors.RESOURCE_NOT_FOUND, exchangeId.toString()));
    }

    private ExchangeMarketDataConnector loadConnector(String exchangeCode) {
        ExchangeVendor vendor = ExchangeVendor.fromCode(exchangeCode);
        return exchangeConnectorRegistry.getConnector(vendor);
    }

    private List<ClientCandle> invokeConnector(ExchangeMarketDataConnector connector, String exchangeCode,
            ExchangeInstrument exchangeInstrument, CanonicalTimeframe timeframe, Instant from, Instant to) {
        try {
            return connector.loadCandles(exchangeInstrument, timeframe, from, to);
        } catch (RuntimeException exception) {
            meterRegistry.counter(Constants.Metrics.HISTORY_RETRIES, createTags(exchangeCode, timeframe))
                .increment();
            throw new ServiceException(Constants.Errors.REMOTE_TIMEOUT, exception.getMessage());
        }
    }

    private List<Candle> mapCandles(List<ClientCandle> clientCandles, Instant toInclusive) {
        if (CollectionUtils.isEmpty(clientCandles)) {
            return List.of();
        }
        return clientCandles.stream()
            .map(clientCandleMapper::map)
            .filter(candle -> !candle.getCloseTime().isAfter(toInclusive))
            .limit(Math.max(1, properties.getBatchLimit()))
            .collect(Collectors.toList());
    }

    private HistoryOperationResult updateCoverageAndBuildResult(UUID exchangeInstrumentId, CanonicalTimeframe timeframe,
            long inserted) {
        Optional<CandleCoverage> existingCoverage = candleRepository.findCoverage(exchangeInstrumentId, timeframe);
        Optional<Candle> earliest = candleRepository.findEarliestCandle(exchangeInstrumentId, timeframe);
        Optional<Candle> latest = candleRepository.findLatestCandle(exchangeInstrumentId, timeframe);

        Instant coverageStart = earliest.map(Candle::getOpenTime)
            .orElseGet(() -> existingCoverage.map(CandleCoverage::getCoverageStart).orElse(null));
        Instant coverageEnd = latest.map(Candle::getCoverageEnd)
            .orElseGet(() -> existingCoverage.map(CandleCoverage::getCoverageEnd).orElse(null));

        if (Objects.nonNull(coverageStart) && Objects.nonNull(coverageEnd)) {
            UUID coverageId = existingCoverage.map(CandleCoverage::getId).orElse(UUID.randomUUID());
            CandleCoverage coverage = CandleCoverage.create(coverageId, exchangeInstrumentId, timeframe, coverageStart, coverageEnd,
                    true, Instant.now());
            candleRepository.upsertCoverage(coverage);
        }

        return HistoryOperationResult.create(exchangeInstrumentId, timeframe, inserted, coverageStart, coverageEnd);
    }

    private void updateLagGauge(UUID exchangeInstrumentId, CanonicalTimeframe timeframe, Optional<Candle> latestCandle) {
        String key = exchangeInstrumentId + ":" + timeframe.name();
        AtomicLong gauge = lagGauges.computeIfAbsent(key, ignored -> {
            AtomicLong holder = new AtomicLong(0L);
            meterRegistry.gauge(Constants.Metrics.HISTORY_LAG_BARS,
                Tags.of("exchangeInstrumentId", exchangeInstrumentId.toString(), "timeframe", timeframe.name()), holder);
            return holder;
        });
        long lag = latestCandle.map(candle -> computeLag(candle.getCloseTime(), timeframe)).orElse(0L);
        gauge.set(lag);
    }

    private long computeLag(Instant latestClose, CanonicalTimeframe timeframe) {
        if (Objects.isNull(latestClose)) {
            return 0L;
        }
        Instant now = Instant.now();
        if (!now.isAfter(latestClose)) {
            return 0L;
        }
        Duration duration = Duration.between(latestClose, now);
        return duration.dividedBy(timeframe.getDuration());
    }

    private String[] createTags(String exchangeCode, CanonicalTimeframe timeframe) {
        return new String[] {
            "exchange", exchangeCode,
            "timeframe", timeframe.name()
        };
    }
}
