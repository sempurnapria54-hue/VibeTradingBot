package com.example.vibetradingbot.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.example.vibetradingbot.domain.enums.ExchangeStatus;
import com.example.vibetradingbot.exception.ServiceException;
import com.example.vibetradingbot.util.Constants;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Доменная модель биржи.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
public class Exchange {

    private UUID id;
    private String code;
    private String name;
    private ExchangeStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    @Builder
    private Exchange(UUID id, String code, String name, ExchangeStatus status, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, Constants.Validation.NULL_IDENTIFIER);
        this.code = Objects.requireNonNull(code, Constants.Validation.NULL_IDENTIFIER);
        this.name = Objects.requireNonNull(name, Constants.Validation.NULL_IDENTIFIER);
        this.status = Objects.requireNonNull(status, Constants.Validation.NULL_IDENTIFIER);
        this.createdAt = Objects.requireNonNull(createdAt, Constants.Validation.NULL_IDENTIFIER);
        this.updatedAt = Objects.requireNonNull(updatedAt, Constants.Validation.NULL_IDENTIFIER);
    }

    public static Exchange create(UUID id, String code, String name, ExchangeStatus status, Instant createdAt, Instant updatedAt) {
        return Exchange.builder()
            .id(id)
            .code(code)
            .name(name)
            .status(status)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }

    public void markInactive() {
        if (Objects.isNull(status)) {
            throw new ServiceException(Constants.Errors.DOMAIN_VALIDATION_FAILED, Constants.Validation.NULL_IDENTIFIER);
        }
        this.status = ExchangeStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }
}
