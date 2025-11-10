package com.example.tradingbot.domain.service;

import com.example.tradingbot.domain.model.Instrument;
import com.example.tradingbot.persistence.repository.InstrumentRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstrumentService {

    private final InstrumentRepository repository;

    public InstrumentService(InstrumentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<Instrument> findByName(String name) {
        return repository.findByName(name);
    }

    @Transactional
    public Instrument register(Instrument instrument) {
        return repository.saveIfAbsent(instrument);
    }

    @Transactional(readOnly = true)
    public List<Instrument> listAll() {
        return repository.listAll();
    }
}
