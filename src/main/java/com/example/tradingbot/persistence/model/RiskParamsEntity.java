package com.example.tradingbot.persistence.model;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RiskParamsEntity {
    private Long id;
    private String version;
    private String canonicalJson;
    private Instant createdAt;
    private String createdBy;
    private Boolean isActive;
}
