package com.example.vibetradingbot.service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.example.vibetradingbot.config.JwtProperties;
import com.example.vibetradingbot.util.Constants;

/**
 * Сервис генерации JWT токенов.
 */
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public JwtService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(String subject, List<String> roles) {
        return generateToken(subject, roles, false);
    }

    public String generateRefreshToken(String subject, List<String> roles) {
        return generateToken(subject, roles, true);
    }

    private String generateToken(String subject, List<String> roles, boolean refresh) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = refresh
            ? issuedAt.plus(jwtProperties.getRefreshTokenTtl())
            : issuedAt.plus(jwtProperties.getAccessTokenTtl());
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
            .issuer(jwtProperties.getIssuer())
            .subject(subject)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .claim(Constants.Security.CLAIM_ROLES, Objects.requireNonNull(roles, Constants.Validation.NULL_IDENTIFIER))
            .claim(Constants.Security.CLAIM_TOKEN_TYPE, refresh ? Constants.Security.TOKEN_TYPE_REFRESH : Constants.Security.TOKEN_TYPE_ACCESS)
            .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }
}
