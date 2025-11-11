package com.example.vibetradingbot.api.v1.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * Ответ покрытия данных.
 */
@Getter
@Builder
public class CoverageResponse {

    private List<CoverageDto> coverages;
}
