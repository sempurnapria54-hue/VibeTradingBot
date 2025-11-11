package com.example.vibetradingbot.exception;

import lombok.Getter;

/**
 * Исключение для неожиданных ошибок инфраструктуры.
 */
@Getter
public class UnexpectedException extends RuntimeException {

    private final String code;

    public UnexpectedException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
