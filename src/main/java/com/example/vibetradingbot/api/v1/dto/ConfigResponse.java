package com.example.vibetradingbot.api.v1.dto;

import java.util.List;

import lombok.Builder;
import lombok.Value;

/**
 * DTO ответа конфигурации.
 */
@Value
@Builder
public class ConfigResponse {

    List<CanonicalTimeframeDto> canonicalTimeframes;
    String message;
}
