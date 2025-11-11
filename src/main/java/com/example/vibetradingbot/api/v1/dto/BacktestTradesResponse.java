package com.example.vibetradingbot.api.v1.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ постраничного списка сделок.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestTradesResponse {

    private List<BacktestTradeDto> trades;
    private Integer total;
    private Integer page;
    private Integer size;
}
