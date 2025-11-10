package com.example.tradingbot.persistence.model;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeEntity {
    private Long id;
    private String name;
    private Instant createdAt;
}
