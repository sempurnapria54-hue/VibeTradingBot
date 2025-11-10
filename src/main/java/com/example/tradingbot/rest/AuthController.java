package com.example.tradingbot.rest;

import com.example.tradingbot.api.v1.dto.LoginRequest;
import com.example.tradingbot.api.v1.dto.RefreshRequest;
import com.example.tradingbot.api.v1.dto.TokenResponse;
import com.example.tradingbot.security.model.JwtProperties;
import com.example.tradingbot.security.service.JwtService;
import com.example.tradingbot.security.service.model.TokenPair;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final JwtProperties properties;

    public AuthController(
        AuthenticationManager authenticationManager,
        UserDetailsService userDetailsService,
        JwtService jwtService,
        JwtProperties properties
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.properties = properties;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        TokenPair pair = jwtService.generateTokens(userDetails);
        TokenResponse response = new TokenResponse(
            pair.getAccessToken(),
            pair.getRefreshToken(),
            properties.getAccessTokenTtl().toSeconds(),
            "Bearer"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        if (!jwtService.isRefreshToken(request.refreshToken())) {
            return ResponseEntity.badRequest().build();
        }
        String username = jwtService.parseClaims(request.refreshToken()).getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        TokenPair pair = jwtService.generateTokens(userDetails);
        TokenResponse response = new TokenResponse(
            pair.getAccessToken(),
            pair.getRefreshToken(),
            properties.getAccessTokenTtl().toSeconds(),
            "Bearer"
        );
        return ResponseEntity.ok(response);
    }
}
