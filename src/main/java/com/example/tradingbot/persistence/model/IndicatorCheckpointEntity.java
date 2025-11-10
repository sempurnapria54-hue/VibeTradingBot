package com.example.tradingbot.persistence.model;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndicatorCheckpointEntity {
    private Long id;
    private String indicator;
    private Long historyGroupId;
    private Long indicatorParamsId;
    private String version;
    private Instant lastTs;
    private Instant updatedAt;
}
