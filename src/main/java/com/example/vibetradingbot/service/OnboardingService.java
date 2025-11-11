package com.example.vibetradingbot.service;

import org.springframework.stereotype.Service;

import com.example.vibetradingbot.api.v1.dto.OnboardingResponse;
import com.example.vibetradingbot.domain.model.OnboardingCommand;
import com.example.vibetradingbot.util.Constants;

/**
 * Сервис онбординга связки биржа-инструмент.
 */
@Service
public class OnboardingService {

    public OnboardingResponse onboard(OnboardingCommand command) {
        return OnboardingResponse.builder()
            .exchangeId(command.getExchangeId())
            .instrumentId(command.getInstrumentId())
            .message(Constants.Messages.ONBOARDING_ACCEPTED)
            .build();
    }
}
