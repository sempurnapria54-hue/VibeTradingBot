package com.example.vibetradingbot.api.v1.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ с событиями ордеров.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveEventsResponse {

    private List<LiveEventDto> events;
}
