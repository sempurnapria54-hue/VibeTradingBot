package com.example.vibetradingbot.api.v1.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ для списка последних сигналов.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalLatestResponse {

    private List<SignalEventDto> signals;
}
