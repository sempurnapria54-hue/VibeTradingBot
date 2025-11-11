package com.example.vibetradingbot.api.v1.dto;

import java.util.Map;

import lombok.Builder;
import lombok.Value;

/**
 * DTO канонического таймфрейма.
 */
@Value
@Builder
public class CanonicalTimeframeDto {

    String code;
    int minutes;
    Map<String, String> exchanges;
}
