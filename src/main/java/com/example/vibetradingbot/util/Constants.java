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
        public static final String INDICATOR_FILL_EMA_PATH = "/api/v1/indicators/fill/ema";
        public static final String INDICATOR_FILL_MACD_PATH = "/api/v1/indicators/fill/macd";
        public static final String INDICATOR_FILL_RSI_PATH = "/api/v1/indicators/fill/rsi";
        public static final String INDICATOR_FILL_STOCH_PATH = "/api/v1/indicators/fill/stoch";
        public static final String INDICATOR_FILL_BB_PATH = "/api/v1/indicators/fill/bb";
        public static final String INDICATOR_FILL_OBV_PATH = "/api/v1/indicators/fill/obv";
        public static final String INDICATOR_CHECKPOINT_PATH = "/api/v1/indicators/checkpoint";
        public static final String SIGNALS_GENERATE_PATH = "/api/v1/signals/generate";
        public static final String SIGNALS_GENERATE_RANGE_PATH = "/api/v1/signals/generate-range";
        public static final String SIGNALS_LATEST_PATH = "/api/v1/signals/latest";
        public static final String SIGNALS_CHECKPOINT_PATH = "/api/v1/signals/checkpoint";
        public static final String QUORUM_DECIDE_PATH = "/api/v1/quorum/decide";
        public static final String QUORUM_DECIDE_RANGE_PATH = "/api/v1/quorum/decide-range";
        public static final String QUORUM_LATEST_PATH = "/api/v1/quorum/latest";
        public static final String QUORUM_CHECKPOINT_PATH = "/api/v1/quorum/checkpoint";
        public static final String QUORUM_PARAMS_ACTIVATE_PATH = "/api/v1/quorum/params/activate";
        public static final String BACKTEST_RUN_PATH = "/api/v1/backtest/run";
        public static final String BACKTEST_REPORT_PATH = "/api/v1/backtest/{runId}/report";
        public static final String BACKTEST_EQUITY_PATH = "/api/v1/backtest/{runId}/equity";
        public static final String BACKTEST_TRADES_PATH = "/api/v1/backtest/{runId}/trades";
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
        public static final String INDICATOR_PARAMS_NOT_FOUND = "indicator.params.not.found";
        public static final String INDICATOR_CONFIGURATION_ERROR = "indicator.configuration.error";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Fields {

        public static final String EXCHANGE = "exchange";
        public static final String INSTRUMENT = "instrument";
        public static final String EXCHANGE_INSTRUMENT = "exchange_instrument";
        public static final String CANDLE = "candle";
        public static final String COVERAGE = "candle_coverage";
        public static final String INDICATOR_PARAMS = "indicator_params";
        public static final String INDICATOR_CHECKPOINT = "indicator_checkpoint";
        public static final String EMA_VALUES = "ema_values";
        public static final String MACD_VALUES = "macd_values";
        public static final String RSI_VALUES = "rsi_values";
        public static final String STOCH_VALUES = "stoch_values";
        public static final String BB_VALUES = "bb_values";
        public static final String OBV_VALUES = "obv_values";
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
        public static final String INDICATOR = "indicator";
        public static final String VERSION = "version";
        public static final String TIMEFRAME_CANONICAL = "timeframe_canonical";
        public static final String CANONICAL_JSON = "canonical_json";
        public static final String INDICATOR_PARAMS_ID = "indicator_params_id";
        public static final String CANDLE_ID = "candle_id";
        public static final String LAST_TS = "last_ts";
        public static final String PERIOD = "period";
        public static final String EMA_VALUE = "ema_value";
        public static final String MACD_VALUE = "macd";
        public static final String SIGNAL_VALUE = "signal";
        public static final String HISTOGRAM_VALUE = "histogram";
        public static final String RSI_VALUE = "rsi";
        public static final String STOCH_K_VALUE = "percent_k";
        public static final String STOCH_D_VALUE = "percent_d";
        public static final String BB_BASIS_VALUE = "basis";
        public static final String BB_UPPER_VALUE = "upper";
        public static final String BB_LOWER_VALUE = "lower";
        public static final String BB_PERCENT_B_VALUE = "percent_b";
        public static final String BB_BANDWIDTH_VALUE = "bandwidth";
        public static final String OBV_VALUE = "obv";
        public static final String SIGNAL_PARAMS = "signal_params";
        public static final String SIGNAL_EVENT = "signal_event";
        public static final String SIGNAL_CHECKPOINT = "signal_checkpoint";
        public static final String QUORUM_DECISION_EVENT = "quorum_decision_event";
        public static final String QUORUM_CHECKPOINT = "quorum_checkpoint";
        public static final String SIGNAL_TYPE = "signal_type";
        public static final String DIRECTION = "direction";
        public static final String SCORE = "score";
        public static final String REASON = "reason";
        public static final String TS_UTC = "ts_utc";
        public static final String ACTION = "action";
        public static final String THRESHOLD_ENTER = "threshold_enter";
        public static final String THRESHOLD_EXIT = "threshold_exit";
        public static final String POSITION_STATE = "position_state";
        public static final String SIGNAL_PARAMS_ID = "signal_params_id";
        public static final String BACKTEST_RUN = "backtest_run";
        public static final String BACKTEST_TRADE = "backtest_trade";
        public static final String BACKTEST_EQUITY_POINT = "backtest_equity_point";
        public static final String PERFORMANCE_AGGREGATE = "performance_aggregate";
        public static final String FROM_UTC = "from_utc";
        public static final String TO_UTC = "to_utc";
        public static final String SIGNAL_TYPES = "signal_types";
        public static final String QUORUM_PARAMS_ID = "quorum_params_id";
        public static final String RISK_PARAMS_ID = "risk_params_id";
        public static final String BACKTEST_PARAMS_ID = "backtest_params_id";
        public static final String PARAMS_JSON = "params_json";
        public static final String RUN_ID = "run_id";
        public static final String ENTRY_TS = "entry_ts";
        public static final String EXIT_TS = "exit_ts";
        public static final String SIDE = "side";
        public static final String ENTRY_PRICE = "entry_price";
        public static final String EXIT_PRICE = "exit_price";
        public static final String PNL_R = "pnl_r";
        public static final String FEES = "fees";
        public static final String SLIPPAGE = "slippage";
        public static final String FUNDING = "funding";
        public static final String MAE_R = "mae_r";
        public static final String MFE_R = "mfe_r";
        public static final String WHICH_HIT_FIRST = "which_hit_first";
        public static final String TIME_TO_EVENT_BARS = "time_to_event_bars";
        public static final String EQUITY_R = "equity_r";
        public static final String TRADES = "trades";
        public static final String WINRATE = "winrate";
        public static final String PROFIT_FACTOR = "profit_factor";
        public static final String EXPECTANCY_R = "expectancy_r";
        public static final String MAX_DRAWDOWN_R = "max_drawdown_r";
        public static final String SHARPE = "sharpe";
        public static final String SORTINO = "sortino";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Schemas {

        public static final String CANDLES = "candles";
        public static final String COVERAGE = "coverage";
        public static final String EXCHANGE = "exchange";
        public static final String EXCHANGE_INSTRUMENT = "exchange_instrument";
        public static final String INSTRUMENT = "instrument";
        public static final String INDICATOR = "indicator";
        public static final String SIGNAL = "signal";
        public static final String BACKTEST = "backtest";
        public static final String QUORUM = "quorum";
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
        public static final String INDICATOR_FILL_ACCEPTED = "Расчёт индикатора инициирован.";
        public static final String SIGNAL_GENERATION_ACCEPTED = "Генерация сигналов инициирована.";
        public static final String QUORUM_DECISION_ACCEPTED = "Расчёт кворума инициирован.";
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
        public static final String INDICATORS_PREFIX = "app.indicators";
        public static final String SIGNALS_PREFIX = "app.signals";
        public static final String QUORUM_PREFIX = "app.quorum";
        public static final String BACKTEST_PREFIX = "app.backtest";
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
        public static final String INDICATOR_EMA_INSERTED = "indicators.ema.values.inserted";
        public static final String INDICATOR_EMA_CHECKPOINT_UPDATED = "indicators.ema.checkpoint.updated";
        public static final String INDICATOR_EMA_SKIPPED_WARMUP = "indicators.ema.skipped.warmup";
        public static final String INDICATOR_MACD_INSERTED = "indicators.macd.values.inserted";
        public static final String INDICATOR_MACD_CHECKPOINT_UPDATED = "indicators.macd.checkpoint.updated";
        public static final String INDICATOR_MACD_SKIPPED_WARMUP = "indicators.macd.skipped.warmup";
        public static final String INDICATOR_RSI_INSERTED = "indicators.rsi.values.inserted";
        public static final String INDICATOR_RSI_CHECKPOINT_UPDATED = "indicators.rsi.checkpoint.updated";
        public static final String INDICATOR_RSI_SKIPPED_WARMUP = "indicators.rsi.skipped.warmup";
        public static final String INDICATOR_STOCH_INSERTED = "indicators.stoch.values.inserted";
        public static final String INDICATOR_STOCH_CHECKPOINT_UPDATED = "indicators.stoch.checkpoint.updated";
        public static final String INDICATOR_STOCH_SKIPPED_WARMUP = "indicators.stoch.skipped.warmup";
        public static final String INDICATOR_BB_INSERTED = "indicators.bb.values.inserted";
        public static final String INDICATOR_BB_CHECKPOINT_UPDATED = "indicators.bb.checkpoint.updated";
        public static final String INDICATOR_BB_SKIPPED_WARMUP = "indicators.bb.skipped.warmup";
        public static final String INDICATOR_OBV_INSERTED = "indicators.obv.values.inserted";
        public static final String INDICATOR_OBV_CHECKPOINT_UPDATED = "indicators.obv.checkpoint.updated";
        public static final String INDICATOR_OBV_SKIPPED_WARMUP = "indicators.obv.skipped.warmup";
        public static final String SIGNALS_GENERATED_TOTAL = "signals.generated.total";
        public static final String SIGNALS_DEBOUNCE_SKIP = "signals.debounce.skip";
        public static final String SIGNALS_COOLDOWN_SKIP = "signals.cooldown.skip";
        public static final String SIGNALS_CHECKPOINT_UPDATED = "signals.checkpoint.updated";
        public static final String QUORUM_DECISIONS_TOTAL = "quorum.decisions.total";
        public static final String QUORUM_SCORE_RAW = "quorum.score.raw";
        public static final String QUORUM_SCORE_FILTERED = "quorum.score.filtered";
        public static final String QUORUM_CHECKPOINT_UPDATED = "quorum.checkpoint.updated";
        public static final String BACKTEST_TRADES_TOTAL = "backtest.trades.total";
        public static final String BACKTEST_TRADES_TP = "backtest.trades.tp";
        public static final String BACKTEST_TRADES_SL = "backtest.trades.sl";
        public static final String BACKTEST_TRADES_TIME = "backtest.trades.time";
        public static final String BACKTEST_EQUITY_MAX_DD = "backtest.equity.maxdd_r";
        public static final String BACKTEST_EQUITY_FINAL = "backtest.equity.final_r";
        public static final String BACKTEST_RUN_DURATION = "backtest.run.duration_ms";
        public static final String BACKTEST_RUN_BARS_PROCESSED = "backtest.run.bars_processed";
    }
}
