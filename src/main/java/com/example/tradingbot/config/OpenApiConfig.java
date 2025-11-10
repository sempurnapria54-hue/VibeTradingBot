package com.example.tradingbot.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openApi() {
        SecurityScheme bearer = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");
        return new OpenAPI()
            .info(new Info().title("Trading Bot API").version("v1"))
            .components(new Components().addSecuritySchemes(SECURITY_SCHEME, bearer))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME));
    }
}
