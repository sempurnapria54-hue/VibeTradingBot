package com.example.vibetradingbot.api.v1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vibetradingbot.api.v1.dto.ConfigResponse;
import com.example.vibetradingbot.service.ConfigService;
import com.example.vibetradingbot.util.Constants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Контроллер конфигурации.
 */
@RestController
@Tag(name = "Config")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping(Constants.Api.CONFIG_PATH)
    @Operation(summary = "Получить конфигурацию канонических таймфреймов")
    public ResponseEntity<ConfigResponse> getConfig() {
        return ResponseEntity.ok(configService.getConfig());
    }
}
