package com.example.tradingbot.client.okx.model;

import java.util.List;

import lombok.Data;

@Data
public class OkxSymbolResponse {
    private String code;
    private String msg;
    private List<OkxSymbol> data;

    @Data
    public static class OkxSymbol {
        private String instId;
        private String instType;
        private String category;
        private String lotSz;
        private String minSz;
        private String tickSz;
        private String lever;
    }
}
