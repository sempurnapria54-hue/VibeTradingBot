package com.example.tradingbot.client.okx.model;

import java.util.List;

import lombok.Data;

@Data
public class OkxCandleResponse {
    private String code;
    private String msg;
    private List<List<String>> data;
}
