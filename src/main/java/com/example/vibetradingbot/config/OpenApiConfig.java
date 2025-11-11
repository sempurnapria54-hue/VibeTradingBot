package com.example.vibetradingbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.vibetradingbot.util.Constants;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Конфигурация OpenAPI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vibeTradingBotApi() {
        SecurityScheme securityScheme = new SecurityScheme()
            .name(Constants.Api.SECURITY_SCHEME_NAME)
            .type(SecurityScheme.Type.HTTP)
            .scheme(Constants.Swagger.BEARER_SCHEME)
            .bearerFormat(Constants.Swagger.BEARER_FORMAT);
        return new OpenAPI()
            .info(new Info()
                .title(Constants.Swagger.TITLE)
                .description(Constants.Swagger.DESCRIPTION)
                .version(Constants.Swagger.VERSION))
            .components(new Components().addSecuritySchemes(Constants.Api.SECURITY_SCHEME_NAME, securityScheme))
            .addSecurityItem(new SecurityRequirement().addList(Constants.Api.SECURITY_SCHEME_NAME));
    }
}
