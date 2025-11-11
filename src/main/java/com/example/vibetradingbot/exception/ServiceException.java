package com.example.vibetradingbot.exception;

import lombok.Getter;

/**
 * Исключение прикладного уровня с кодом ошибки.
 */
@Getter
public class ServiceException extends RuntimeException {

    private final String code;

    public ServiceException(String code, String message) {
        super(message);
        this.code = code;
    }
}
