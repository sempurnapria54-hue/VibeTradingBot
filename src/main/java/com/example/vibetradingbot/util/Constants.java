package com.example.vibetradingbot.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Набор констант проекта, разбитый по доменам.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Api {

        public static final String BASE_PATH = "/api/v1";
        public static final String HEALTH_PATH = "/api/v1/health";
        public static final String CONFIG_PATH = "/api/v1/config";
        public static final String ONBOARDING_PATH = "/api/v1/onboarding";
        public static final String DEV_TOKEN_PATH = "/api/v1/dev/token";
        public static final String HISTORY_FILL_PATH = "/api/v1/history/fill";
        public static final String HISTORY_FILL_INCREMENTAL_PATH = "/api/v1/history/fill/incremental";
        public static final String HISTORY_FILL_RANGE_PATH = "/api/v1/history/fill/range";
        public static final String COVERAGE_PATH = "/api/v1/coverage";
        public static final String CONSISTENCY_SCAN_PATH = "/api/v1/consistency/scan";
        public static final String CONSISTENCY_FILL_MISSING_PATH = "/api/v1/consistency/fill-missing";
        public static final String SWAGGER_PACKAGE = "com.example.vibetradingbot.api";
        public static final String SECURITY_SCHEME_NAME = "jwt";
        public static final String SWAGGER_DOCS_PATTERN = "/v3/api-docs/**";
        public static final String SWAGGER_UI_PATTERN = "/swagger-ui/**";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Errors {

        public static final String INVALID_CLIENT_PAYLOAD = "client.payload.invalid";
        public static final String DOMAIN_VALIDATION_FAILED = "domain.validation.failed";
        public static final String RESOURCE_NOT_FOUND = "resource.not.found";
        public static final String SECURITY_UNAUTHORIZED = "security.unauthorized";
        public static final String CONFIGURATION_ERROR = "configuration.error";
        public static final String UNEXPECTED_ERROR = "unexpected.error";
        public static final String REMOTE_TIMEOUT = "remote.timeout";
        public static final String REMOTE_RATE_LIMIT = "remote.rate.limit";
        public static final String PERSISTENCE_ERROR = "persistence.error";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Fields {

        public static final String EXCHANGE = "exchange";
        public static final String INSTRUMENT = "instrument";
        public static final String EXCHANGE_INSTRUMENT = "exchange_instrument";
        public static final String CANDLE = "candle";
        public static final String COVERAGE = "candle_coverage";
        public static final String ID = "id";
        public static final String CODE = "code";
        public static final String NAME = "name";
        public static final String STATUS = "status";
        public static final String TIMEFRAME = "timeframe";
        public static final String EXCHANGE_ID = "exchange_id";
        public static final String INSTRUMENT_ID = "instrument_id";
        public static final String EXCHANGE_INSTRUMENT_ID = "exchange_instrument_id";
        public static final String COVERAGE_END_UTC = "coverage_end_utc";
        public static final String COVERAGE_START_UTC = "coverage_start_utc";
        public static final String OPEN_TIME_UTC = "open_time_utc";
        public static final String CLOSE_TIME_UTC = "close_time_utc";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
        public static final String EXCHANGE_SYMBOL = "exchange_symbol";
        public static final String MIN_TRADE_QUANTITY = "min_trade_quantity";
        public static final String TICK_SIZE = "tick_size";
        public static final String MAKER_FEE_RATE = "maker_fee_rate";
        public static final String TAKER_FEE_RATE = "taker_fee_rate";
        public static final String VOLUME = "volume";
        public static final String TRADES_COUNT = "trades_count";
        public static final String OPEN_PRICE = "open_price";
        public static final String CLOSE_PRICE = "close_price";
        public static final String HIGH_PRICE = "high_price";
        public static final String LOW_PRICE = "low_price";
        public static final String IS_COMPLETE = "is_complete";
        public static final String IS_ACTIVE = "is_active";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Schemas {

        public static final String CANDLES = "candles";
        public static final String COVERAGE = "coverage";
        public static final String EXCHANGE = "exchange";
        public static final String EXCHANGE_INSTRUMENT = "exchange_instrument";
        public static final String INSTRUMENT = "instrument";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Profiles {

        public static final String DEV = "dev";
        public static final String PROD = "prod";
        public static final String TEST = "test";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Security {

        public static final String AUTHORITY_TRADER = "ROLE_TRADER";
        public static final String AUTHORITY_VIEWER = "ROLE_VIEWER";
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String CLAIM_ROLES = "roles";
        public static final String CLAIM_SUBJECT = "sub";
        public static final String CLAIM_TOKEN_TYPE = "token_type";
        public static final String TOKEN_TYPE_ACCESS = "access";
        public static final String TOKEN_TYPE_REFRESH = "refresh";
        public static final String DEV_USERNAME = "dev";
        public static final String DEV_PASSWORD = "dev-password";
        public static final String HMAC_ALGORITHM = "HmacSHA256";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Swagger {

        public static final String TITLE = "Vibe Trading Bot API";
        public static final String DESCRIPTION = "Базовые эндпоинты для здоровья, конфигурации и онбординга.";
        public static final String VERSION = "1.0.0";
        public static final String BEARER_SCHEME = "bearer";
        public static final String BEARER_FORMAT = "JWT";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Messages {

        public static final String HEALTH_OK = "Сервис доступен.";
        public static final String HEALTH_STATUS_UP = "UP";
        public static final String CONFIG_LOADED = "Конфигурация загружена.";
        public static final String ONBOARDING_ACCEPTED = "Заявка на онбординг принята.";
        public static final String CONFIG_LOAD_FAILED = "Не удалось загрузить конфигурацию таймфреймов.";
        public static final String HISTORY_FILL_COMPLETED = "Загрузка истории завершена.";
        public static final String CONSISTENCY_SCAN_COMPLETED = "Сканирование целостности выполнено.";
        public static final String CONSISTENCY_FILL_COMPLETED = "Докачка пропусков инициирована.";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Validation {

        public static final String NULL_CLIENT_CANDLE = "Внешняя свеча отсутствует.";
        public static final String NEGATIVE_VOLUME = "Объем не может быть отрицательным.";
        public static final String INVALID_TIME_RANGE = "Диапазон времени свечи некорректен.";
        public static final String NULL_IDENTIFIER = "Идентификатор не может быть пустым.";
        public static final String NULL_TIMEFRAME = "Таймфрейм не может быть пустым.";
        public static final String UNKNOWN_TIMEFRAME = "Неизвестный таймфрейм.";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Config {

        public static final String CANONICAL_TIMEFRAMES_PREFIX = "canonical-timeframes";
        public static final String DECIMAL_PREFIX = "decimal";
        public static final String SECURITY_JWT_PREFIX = "security.jwt";
        public static final String HISTORY_PREFIX = "app.history";
        public static final String CONSISTENCY_PREFIX = "app.consistency";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Metrics {

        public static final String HISTORY_CANDLES_INSERTED = "history.candles.inserted";
        public static final String HISTORY_CALLS = "history.calls";
        public static final String HISTORY_RETRIES = "history.retries";
        public static final String HISTORY_RATE_LIMITED = "history.rate_limited";
        public static final String HISTORY_LAG_BARS = "history.lag.bars";
        public static final String CONSISTENCY_GAPS_FOUND = "consistency.gaps.found";
        public static final String CONSISTENCY_GAPS_FILLED = "consistency.gaps.filled";
        public static final String CONSISTENCY_SCANS_TOTAL = "consistency.scans.total";
    }
}
