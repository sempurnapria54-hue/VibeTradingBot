package com.example.tradingbot.persistence.model;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignalParamsEntity {
    private Long id;
    private String type;
    private String timeframeCanonical;
    private String version;
    private String canonicalJson;
    private Instant createdAt;
    private String createdBy;
    private Boolean isActive;
}
