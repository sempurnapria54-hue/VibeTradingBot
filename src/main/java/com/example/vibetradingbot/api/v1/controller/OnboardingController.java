package com.example.vibetradingbot.api.v1.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.vibetradingbot.api.v1.dto.OnboardingRequest;
import com.example.vibetradingbot.api.v1.dto.OnboardingResponse;
import com.example.vibetradingbot.api.v1.mapper.ApiToDomainMapper;
import com.example.vibetradingbot.service.OnboardingService;
import com.example.vibetradingbot.util.Constants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Контроллер онбординга связки биржа-инструмент.
 */
@RestController
@Tag(name = "Onboarding")
public class OnboardingController {

    private final ApiToDomainMapper apiToDomainMapper;
    private final OnboardingService onboardingService;

    public OnboardingController(ApiToDomainMapper apiToDomainMapper, OnboardingService onboardingService) {
        this.apiToDomainMapper = apiToDomainMapper;
        this.onboardingService = onboardingService;
    }

    @PostMapping(Constants.Api.ONBOARDING_PATH)
    @Operation(summary = "Создать заявку на онбординг")
    public ResponseEntity<OnboardingResponse> onboard(@Valid @RequestBody OnboardingRequest request) {
        return ResponseEntity.ok(onboardingService.onboard(apiToDomainMapper.toCommand(request)));
    }
}
