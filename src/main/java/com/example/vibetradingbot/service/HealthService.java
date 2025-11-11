package com.example.vibetradingbot.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.example.vibetradingbot.api.v1.dto.HealthResponse;
import com.example.vibetradingbot.util.Constants;

/**
 * Сервис проверки состояния приложения.
 */
@Service
public class HealthService {

    public HealthResponse getStatus() {
        return HealthResponse.builder()
            .status(Constants.Messages.HEALTH_STATUS_UP)
            .message(Constants.Messages.HEALTH_OK)
            .timestamp(Instant.now())
            .build();
    }
}
