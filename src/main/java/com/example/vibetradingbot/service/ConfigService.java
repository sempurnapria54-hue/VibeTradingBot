package com.example.vibetradingbot.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.vibetradingbot.api.v1.dto.CanonicalTimeframeDto;
import com.example.vibetradingbot.api.v1.dto.ConfigResponse;
import com.example.vibetradingbot.api.v1.mapper.DomainToApiMapper;
import com.example.vibetradingbot.domain.service.CanonicalTimeframeService;
import com.example.vibetradingbot.util.Constants;

/**
 * Сервис предоставления конфигурации.
 */
@Service
public class ConfigService {

    private final CanonicalTimeframeService canonicalTimeframeService;
    private final DomainToApiMapper domainToApiMapper;

    public ConfigService(CanonicalTimeframeService canonicalTimeframeService, DomainToApiMapper domainToApiMapper) {
        this.canonicalTimeframeService = canonicalTimeframeService;
        this.domainToApiMapper = domainToApiMapper;
    }

    public ConfigResponse getConfig() {
        List<CanonicalTimeframeDto> timeframes = domainToApiMapper.toDtoList(canonicalTimeframeService.getDefinitions().values());
        return ConfigResponse.builder()
            .canonicalTimeframes(timeframes)
            .message(Constants.Messages.CONFIG_LOADED)
            .build();
    }
}
