package com.example.vibetradingbot.api.v1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vibetradingbot.api.v1.dto.HealthResponse;
import com.example.vibetradingbot.service.HealthService;
import com.example.vibetradingbot.util.Constants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Контроллер проверки здоровья.
 */
@RestController
@Tag(name = "Health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping(Constants.Api.HEALTH_PATH)
    @Operation(summary = "Получить состояние сервиса")
    public ResponseEntity<HealthResponse> getHealth() {
        return ResponseEntity.ok(healthService.getStatus());
    }
}
