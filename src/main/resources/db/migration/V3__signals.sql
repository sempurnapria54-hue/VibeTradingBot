-- Схема и таблицы для сигналов торговых стратегий.
CREATE SCHEMA IF NOT EXISTS signal;
COMMENT ON SCHEMA signal IS 'Схема хранения параметров и событий сигналов.';

-- Таблица параметров сигналов (append-only).
CREATE TABLE IF NOT EXISTS signal.signal_params
(
    id BIGSERIAL PRIMARY KEY,
    signal_type TEXT NOT NULL,
    timeframe_canonical TEXT NOT NULL,
    version TEXT NOT NULL,
    canonical_json JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by TEXT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);
COMMENT ON TABLE signal.signal_params IS 'Реестр параметров сигналов (append-only).';
COMMENT ON COLUMN signal.signal_params.id IS 'Уникальный идентификатор профиля параметров сигнала.';
COMMENT ON COLUMN signal.signal_params.signal_type IS 'Тип сигнала (EMA_TREND, MACD_IMPULSE и др.).';
COMMENT ON COLUMN signal.signal_params.timeframe_canonical IS 'Канонический таймфрейм, для которого определены параметры.';
COMMENT ON COLUMN signal.signal_params.version IS 'Версия правил генерации сигнала.';
COMMENT ON COLUMN signal.signal_params.canonical_json IS 'Канонизированный JSON с параметрами сигнала.';
COMMENT ON COLUMN signal.signal_params.created_at IS 'Момент фиксации параметров сигнала (UTC).';
COMMENT ON COLUMN signal.signal_params.created_by IS 'Идентификатор автора/процесса, создавшего запись.';
COMMENT ON COLUMN signal.signal_params.is_active IS 'Флаг активности профиля параметров сигнала.';

CREATE UNIQUE INDEX IF NOT EXISTS ux_signal_params_unique
    ON signal.signal_params (signal_type, timeframe_canonical, version, canonical_json);
CREATE INDEX IF NOT EXISTS ix_signal_params_lookup
    ON signal.signal_params (signal_type, timeframe_canonical, version);

-- Таблица событий сигналов.
CREATE TABLE IF NOT EXISTS signal.signal_event
(
    id BIGSERIAL PRIMARY KEY,
    exchange_instrument_id UUID NOT NULL REFERENCES exchange_instrument.exchange_instrument (id),
    timeframe_canonical TEXT NOT NULL,
    ts_utc TIMESTAMPTZ NOT NULL,
    signal_type TEXT NOT NULL,
    direction TEXT NOT NULL,
    score NUMERIC(10, 6) NOT NULL,
    reason JSONB NOT NULL,
    signal_params_id BIGINT NOT NULL REFERENCES signal.signal_params (id),
    version INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (exchange_instrument_id, timeframe_canonical, ts_utc, signal_type, signal_params_id, version)
);
COMMENT ON TABLE signal.signal_event IS 'События генерации сигналов с explainability-структурой.';
COMMENT ON COLUMN signal.signal_event.id IS 'Уникальный идентификатор события сигнала.';
COMMENT ON COLUMN signal.signal_event.exchange_instrument_id IS 'Связка биржи и инструмента для которой сгенерирован сигнал.';
COMMENT ON COLUMN signal.signal_event.timeframe_canonical IS 'Канонический таймфрейм свечи (UTC).';
COMMENT ON COLUMN signal.signal_event.ts_utc IS 'Метка закрытия свечи, на которой сгенерирован сигнал (UTC).';
COMMENT ON COLUMN signal.signal_event.signal_type IS 'Тип сигнала (EMA_TREND, MACD_IMPULSE и др.).';
COMMENT ON COLUMN signal.signal_event.direction IS 'Направление сигнала (LONG, SHORT, NEUTRAL).';
COMMENT ON COLUMN signal.signal_event.score IS 'Сводный скор сигнала в диапазоне [-1..+1].';
COMMENT ON COLUMN signal.signal_event.reason IS 'Структурированное объяснение сигнала (JSONB).';
COMMENT ON COLUMN signal.signal_event.signal_params_id IS 'Ссылка на параметры сигнала.';
COMMENT ON COLUMN signal.signal_event.version IS 'Версия алгоритма генерации сигнала.';
COMMENT ON COLUMN signal.signal_event.created_at IS 'Момент вставки события сигнала.';

CREATE INDEX IF NOT EXISTS ix_signal_event_series
    ON signal.signal_event (exchange_instrument_id, timeframe_canonical, ts_utc);
CREATE INDEX IF NOT EXISTS ix_signal_event_type
    ON signal.signal_event (signal_type);

-- Таблица чекпоинтов генерации сигналов.
CREATE TABLE IF NOT EXISTS signal.signal_checkpoint
(
    signal_type TEXT NOT NULL,
    timeframe_canonical TEXT NOT NULL,
    exchange_instrument_id UUID NOT NULL REFERENCES exchange_instrument.exchange_instrument (id),
    signal_params_id BIGINT NOT NULL REFERENCES signal.signal_params (id),
    version INTEGER NOT NULL,
    last_ts TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (signal_type, timeframe_canonical, exchange_instrument_id, signal_params_id, version)
);
COMMENT ON TABLE signal.signal_checkpoint IS 'Чекпоинты генерации сигналов по сериям.';
COMMENT ON COLUMN signal.signal_checkpoint.signal_type IS 'Тип сигнала (EMA_TREND, MACD_IMPULSE и др.).';
COMMENT ON COLUMN signal.signal_checkpoint.timeframe_canonical IS 'Канонический таймфрейм серии.';
COMMENT ON COLUMN signal.signal_checkpoint.exchange_instrument_id IS 'Связка биржи и инструмента для серии.';
COMMENT ON COLUMN signal.signal_checkpoint.signal_params_id IS 'Ссылка на параметры сигнала.';
COMMENT ON COLUMN signal.signal_checkpoint.version IS 'Версия алгоритма генерации сигнала.';
COMMENT ON COLUMN signal.signal_checkpoint.last_ts IS 'Последнее обработанное время свечи (UTC).';
COMMENT ON COLUMN signal.signal_checkpoint.updated_at IS 'Момент обновления чекпоинта.';

CREATE INDEX IF NOT EXISTS ix_signal_checkpoint_series
    ON signal.signal_checkpoint (exchange_instrument_id, timeframe_canonical);
