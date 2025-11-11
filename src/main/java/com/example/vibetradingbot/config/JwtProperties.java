package com.example.vibetradingbot.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.example.vibetradingbot.util.Constants;

import lombok.Getter;
import lombok.Setter;

/**
 * Свойства конфигурации JWT.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = Constants.Config.SECURITY_JWT_PREFIX)
public class JwtProperties {

    private String issuer;
    private String secret;
    private Duration accessTokenTtl;
    private Duration refreshTokenTtl;
}
