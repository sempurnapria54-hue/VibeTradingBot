package com.example.vibetradingbot.api.v1.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

/**
 * Запрос на активацию новых параметров кворума.
 */
@Getter
@Setter
public class QuorumParamsActivateRequest {

    @NotBlank
    private String version;

    @NotBlank
    private String timeframe;

    @NotNull
    private Map<String, Object> config;

    private String createdBy;
}
