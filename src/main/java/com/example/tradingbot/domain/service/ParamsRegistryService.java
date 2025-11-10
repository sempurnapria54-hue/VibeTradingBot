package com.example.tradingbot.domain.service;

import com.example.tradingbot.domain.model.params.ExchangeParams;
import com.example.tradingbot.domain.model.params.IndicatorParams;
import com.example.tradingbot.domain.model.params.QuorumParams;
import com.example.tradingbot.domain.model.params.RiskParams;
import com.example.tradingbot.domain.model.params.SignalParams;
import com.example.tradingbot.persistence.repository.ParamsRegistryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParamsRegistryService {

    private final ParamsRegistryRepository repository;

    public ParamsRegistryService(ParamsRegistryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public IndicatorParams resolveIndicator(IndicatorParams params) {
        return repository.resolveIndicator(params);
    }

    @Transactional
    public SignalParams resolveSignal(SignalParams params) {
        return repository.resolveSignal(params);
    }

    @Transactional
    public QuorumParams resolveQuorum(QuorumParams params) {
        return repository.resolveQuorum(params);
    }

    @Transactional
    public RiskParams resolveRisk(RiskParams params) {
        return repository.resolveRisk(params);
    }

    @Transactional
    public ExchangeParams resolveExchange(ExchangeParams params) {
        return repository.resolveExchange(params);
    }
}
