package com.example.vibetradingbot.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Страница сделок бэктеста.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class BacktestTradePage {

    private List<BacktestTrade> trades;
    private Integer total;
    private Integer page;
    private Integer size;

    @Builder
    private BacktestTradePage(List<BacktestTrade> trades, Integer total, Integer page, Integer size) {
        this.trades = trades == null ? new ArrayList<>() : new ArrayList<>(trades);
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public static BacktestTradePage create(List<BacktestTrade> trades, Integer total, Integer page, Integer size) {
        return BacktestTradePage.builder()
            .trades(trades)
            .total(total)
            .page(page)
            .size(size)
            .build();
    }

    public List<BacktestTrade> getTrades() {
        if (trades == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(trades);
    }
}
