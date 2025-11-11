package com.example.vibetradingbot.api.v1.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.vibetradingbot.api.v1.dto.TokenRequest;
import com.example.vibetradingbot.api.v1.dto.TokenResponse;
import com.example.vibetradingbot.service.JwtService;
import com.example.vibetradingbot.util.Constants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Контроллер выдачи dev-токенов.
 */
@RestController
@Tag(name = "DevToken")
public class DevTokenController {

    private final JwtService jwtService;

    public DevTokenController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping(Constants.Api.DEV_TOKEN_PATH)
    @Operation(summary = "Получить тестовые токены")
    public ResponseEntity<TokenResponse> issueTokens(@Valid @RequestBody TokenRequest request) {
        List<String> roles = List.of(Constants.Security.AUTHORITY_TRADER);
        return ResponseEntity.ok(TokenResponse.builder()
            .accessToken(jwtService.generateAccessToken(request.getUsername(), roles))
            .refreshToken(jwtService.generateRefreshToken(request.getUsername(), roles))
            .build());
    }
}
