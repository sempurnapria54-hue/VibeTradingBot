package com.example.tradingbot.security.service;

import com.example.tradingbot.security.model.JwtProperties;
import com.example.tradingbot.security.service.model.TokenPair;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.KeyPair;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final KeyPair keyPair;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.keyPair = RsaKeyService.loadOrCreate(properties.getPrivateKey(), properties.getPublicKey());
    }

    public TokenPair generateTokens(UserDetails user) {
        String access = buildToken(user, properties.getAccessTokenTtl().toSeconds(), "access");
        String refresh = buildToken(user, properties.getRefreshTokenTtl().toSeconds(), "refresh");
        return new TokenPair(access, refresh);
    }

    public Authentication toAuthentication(String token) {
        Claims claims = parseClaims(token);
        String username = claims.getSubject();
        Collection<GrantedAuthority> authorities = extractAuthorities(claims);
        return new UsernamePasswordAuthenticationToken(username, token, authorities);
    }

    public boolean isRefreshToken(String token) {
        Claims claims = parseClaims(token);
        return "refresh".equals(claims.get("token_type"));
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(keyPair.getPublic())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private String buildToken(UserDetails user, long ttlSeconds, String tokenType) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(ttlSeconds);
        Map<String, Object> claims = Map.of(
            "roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()),
            "token_type", tokenType
        );
        String issuer = properties.getIssuer() != null ? properties.getIssuer() : "trading-bot";
        return Jwts.builder()
            .subject(user.getUsername())
            .issuer(issuer)
            .claims(claims)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
            .compact();
    }

    private Collection<GrantedAuthority> extractAuthorities(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof Collection<?> collection) {
            return collection.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> (GrantedAuthority) () -> role)
                .collect(Collectors.toList());
        }
        return java.util.List.of();
    }
}
