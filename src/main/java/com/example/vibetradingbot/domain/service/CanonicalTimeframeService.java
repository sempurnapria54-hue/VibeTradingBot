package com.example.vibetradingbot.domain.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import com.example.vibetradingbot.config.CanonicalTimeframeProperties;
import com.example.vibetradingbot.domain.enums.CanonicalTimeframe;
import com.example.vibetradingbot.domain.model.CanonicalTimeframeDefinition;
import com.example.vibetradingbot.exception.UnexpectedException;
import com.example.vibetradingbot.util.Constants;

import lombok.Getter;
import lombok.Setter;

/**
 * Сервис загрузки конфигурации канонических таймфреймов.
 */
@Service
public class CanonicalTimeframeService {

    private final CanonicalTimeframeProperties properties;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private Map<CanonicalTimeframe, CanonicalTimeframeDefinition> cachedDefinitions = Collections.emptyMap();

    public CanonicalTimeframeService(CanonicalTimeframeProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.cachedDefinitions = loadDefinitions();
    }

    public Map<CanonicalTimeframe, CanonicalTimeframeDefinition> getDefinitions() {
        return cachedDefinitions;
    }

    private Map<CanonicalTimeframe, CanonicalTimeframeDefinition> loadDefinitions() {
        Resource resource = resourceLoader.getResource(properties.getResource());
        try (InputStream inputStream = resource.getInputStream()) {
            CanonicalTimeframeConfig config = yamlMapper.readValue(inputStream, CanonicalTimeframeConfig.class);
            return config.getCanonicalTimeframes().stream()
                .collect(Collectors.toMap(
                    entry -> CanonicalTimeframe.fromCode(entry.getCode()),
                    entry -> new CanonicalTimeframeDefinition(
                        CanonicalTimeframe.fromCode(entry.getCode()),
                        entry.getMinutes(),
                        entry.getExchanges())));
        } catch (IOException exception) {
            throw new UnexpectedException(Constants.Errors.CONFIGURATION_ERROR, Constants.Messages.CONFIG_LOAD_FAILED, exception);
        }
    }

    @Getter
    @Setter
    private static class CanonicalTimeframeConfig {

        private List<CanonicalTimeframeEntry> canonicalTimeframes = Collections.emptyList();
    }

    @Getter
    @Setter
    private static class CanonicalTimeframeEntry {

        private String code;
        private int minutes;
        private Map<String, String> exchanges = Collections.emptyMap();
    }
}
