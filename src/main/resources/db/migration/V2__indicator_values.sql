-- Схема и таблицы для параметров и значений индикаторов.
CREATE SCHEMA IF NOT EXISTS indicator;
COMMENT ON SCHEMA indicator IS 'Схема хранения параметров и значений индикаторов.';

-- Таблица параметров индикаторов.
CREATE TABLE IF NOT EXISTS indicator.indicator_params
(
    id BIGSERIAL PRIMARY KEY,
    indicator TEXT NOT NULL,
    timeframe_canonical TEXT NULL,
    version TEXT NOT NULL,
    canonical_json JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by TEXT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);
COMMENT ON TABLE indicator.indicator_params IS 'Реестр параметров индикаторов (append-only).';
COMMENT ON COLUMN indicator.indicator_params.id IS 'Уникальный идентификатор параметров индикатора.';
COMMENT ON COLUMN indicator.indicator_params.indicator IS 'Тип индикатора (EMA, MACD, RSI, STOCH, BB, OBV).';
COMMENT ON COLUMN indicator.indicator_params.timeframe_canonical IS 'Канонический таймфрейм, к которому привязаны параметры.';
COMMENT ON COLUMN indicator.indicator_params.version IS 'Версия параметров индикатора (строковая).';
COMMENT ON COLUMN indicator.indicator_params.canonical_json IS 'Канонизированный JSON параметров индикатора.';
COMMENT ON COLUMN indicator.indicator_params.created_at IS 'Момент фиксации параметров индикатора (UTC).';
COMMENT ON COLUMN indicator.indicator_params.created_by IS 'Идентификатор автора/сервиса, создавшего запись.';
COMMENT ON COLUMN indicator.indicator_params.is_active IS 'Флаг активности профиля параметров.';

CREATE UNIQUE INDEX IF NOT EXISTS ux_indicator_params_unique
    ON indicator.indicator_params (indicator, timeframe_canonical, version, canonical_json);
CREATE INDEX IF NOT EXISTS ix_indicator_params_lookup
    ON indicator.indicator_params (indicator, timeframe_canonical, version);

-- Таблица чекпоинтов индикаторов.
CREATE TABLE IF NOT EXISTS indicator.indicator_checkpoint
(
    indicator TEXT NOT NULL,
    timeframe_canonical TEXT NOT NULL,
    exchange_instrument_id UUID NOT NULL,
    indicator_params_id BIGINT NOT NULL REFERENCES indicator.indicator_params (id),
    version INTEGER NOT NULL,
    last_ts TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (indicator, timeframe_canonical, exchange_instrument_id, indicator_params_id, version)
);
COMMENT ON TABLE indicator.indicator_checkpoint IS 'Чекпоинты расчёта индикаторов по сериям.';
COMMENT ON COLUMN indicator.indicator_checkpoint.indicator IS 'Тип индикатора (EMA, MACD, RSI, STOCH, BB, OBV).';
COMMENT ON COLUMN indicator.indicator_checkpoint.timeframe_canonical IS 'Канонический таймфрейм серии.';
COMMENT ON COLUMN indicator.indicator_checkpoint.exchange_instrument_id IS 'Связка биржи и инструмента для серии.';
COMMENT ON COLUMN indicator.indicator_checkpoint.indicator_params_id IS 'Ссылка на параметры индикатора.';
COMMENT ON COLUMN indicator.indicator_checkpoint.version IS 'Версия алгоритма расчёта индикатора.';
COMMENT ON COLUMN indicator.indicator_checkpoint.last_ts IS 'Последнее рассчитанное время свечи (UTC).';
COMMENT ON COLUMN indicator.indicator_checkpoint.updated_at IS 'Момент обновления чекпоинта.';

-- Таблица значений EMA.
CREATE TABLE IF NOT EXISTS indicator.ema_values
(
    id BIGSERIAL PRIMARY KEY,
    candle_id UUID NOT NULL REFERENCES candles.candle (id),
    indicator_params_id BIGINT NOT NULL REFERENCES indicator.indicator_params (id),
    version INTEGER NOT NULL,
    period INTEGER NOT NULL,
    ema_value NUMERIC(50, 30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (candle_id, indicator_params_id, version, period)
);
COMMENT ON TABLE indicator.ema_values IS 'Значения индикатора EMA по свечам.';
COMMENT ON COLUMN indicator.ema_values.id IS 'Уникальный идентификатор значения EMA.';
COMMENT ON COLUMN indicator.ema_values.candle_id IS 'Свеча, для которой рассчитано значение.';
COMMENT ON COLUMN indicator.ema_values.indicator_params_id IS 'Параметры индикатора EMA.';
COMMENT ON COLUMN indicator.ema_values.version IS 'Версия алгоритма расчёта EMA.';
COMMENT ON COLUMN indicator.ema_values.period IS 'Период EMA из профиля.';
COMMENT ON COLUMN indicator.ema_values.ema_value IS 'Рассчитанное значение EMA.';
COMMENT ON COLUMN indicator.ema_values.created_at IS 'Момент вставки значения EMA.';
CREATE INDEX IF NOT EXISTS ix_ema_values_params_period
    ON indicator.ema_values (indicator_params_id, period);
CREATE INDEX IF NOT EXISTS ix_ema_values_candle
    ON indicator.ema_values (candle_id);

-- Таблица значений MACD.
CREATE TABLE IF NOT EXISTS indicator.macd_values
(
    id BIGSERIAL PRIMARY KEY,
    candle_id UUID NOT NULL REFERENCES candles.candle (id),
    indicator_params_id BIGINT NOT NULL REFERENCES indicator.indicator_params (id),
    version INTEGER NOT NULL,
    macd NUMERIC(50, 30) NOT NULL,
    signal NUMERIC(50, 30) NOT NULL,
    histogram NUMERIC(50, 30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (candle_id, indicator_params_id, version)
);
COMMENT ON TABLE indicator.macd_values IS 'Значения индикатора MACD по свечам.';
COMMENT ON COLUMN indicator.macd_values.id IS 'Уникальный идентификатор значения MACD.';
COMMENT ON COLUMN indicator.macd_values.candle_id IS 'Свеча, для которой рассчитаны значения MACD.';
COMMENT ON COLUMN indicator.macd_values.indicator_params_id IS 'Параметры индикатора MACD.';
COMMENT ON COLUMN indicator.macd_values.version IS 'Версия алгоритма расчёта MACD.';
COMMENT ON COLUMN indicator.macd_values.macd IS 'Значение MACD (разница EMA быстрый - медленный).';
COMMENT ON COLUMN indicator.macd_values.signal IS 'Значение сигнальной линии MACD.';
COMMENT ON COLUMN indicator.macd_values.histogram IS 'Значение гистограммы MACD.';
COMMENT ON COLUMN indicator.macd_values.created_at IS 'Момент вставки значения MACD.';
CREATE INDEX IF NOT EXISTS ix_macd_values_params
    ON indicator.macd_values (indicator_params_id);
CREATE INDEX IF NOT EXISTS ix_macd_values_candle
    ON indicator.macd_values (candle_id);

-- Таблица значений RSI.
CREATE TABLE IF NOT EXISTS indicator.rsi_values
(
    id BIGSERIAL PRIMARY KEY,
    candle_id UUID NOT NULL REFERENCES candles.candle (id),
    indicator_params_id BIGINT NOT NULL REFERENCES indicator.indicator_params (id),
    version INTEGER NOT NULL,
    rsi NUMERIC(50, 30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (candle_id, indicator_params_id, version)
);
COMMENT ON TABLE indicator.rsi_values IS 'Значения индикатора RSI по свечам.';
COMMENT ON COLUMN indicator.rsi_values.id IS 'Уникальный идентификатор значения RSI.';
COMMENT ON COLUMN indicator.rsi_values.candle_id IS 'Свеча, для которой рассчитано значение RSI.';
COMMENT ON COLUMN indicator.rsi_values.indicator_params_id IS 'Параметры индикатора RSI.';
COMMENT ON COLUMN indicator.rsi_values.version IS 'Версия алгоритма расчёта RSI.';
COMMENT ON COLUMN indicator.rsi_values.rsi IS 'Расчитанное значение RSI.';
COMMENT ON COLUMN indicator.rsi_values.created_at IS 'Момент вставки значения RSI.';
CREATE INDEX IF NOT EXISTS ix_rsi_values_params
    ON indicator.rsi_values (indicator_params_id);
CREATE INDEX IF NOT EXISTS ix_rsi_values_candle
    ON indicator.rsi_values (candle_id);

-- Таблица значений Stochastic Oscillator.
CREATE TABLE IF NOT EXISTS indicator.stoch_values
(
    id BIGSERIAL PRIMARY KEY,
    candle_id UUID NOT NULL REFERENCES candles.candle (id),
    indicator_params_id BIGINT NOT NULL REFERENCES indicator.indicator_params (id),
    version INTEGER NOT NULL,
    percent_k NUMERIC(50, 30) NOT NULL,
    percent_d NUMERIC(50, 30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (candle_id, indicator_params_id, version)
);
COMMENT ON TABLE indicator.stoch_values IS 'Значения стохастического осциллятора по свечам.';
COMMENT ON COLUMN indicator.stoch_values.id IS 'Уникальный идентификатор значения стохастика.';
COMMENT ON COLUMN indicator.stoch_values.candle_id IS 'Свеча, для которой рассчитан стохастический осциллятор.';
COMMENT ON COLUMN indicator.stoch_values.indicator_params_id IS 'Параметры стохастического осциллятора.';
COMMENT ON COLUMN indicator.stoch_values.version IS 'Версия алгоритма расчёта стохастика.';
COMMENT ON COLUMN indicator.stoch_values.percent_k IS 'Значение %K стохастического осциллятора.';
COMMENT ON COLUMN indicator.stoch_values.percent_d IS 'Значение %D стохастического осциллятора.';
COMMENT ON COLUMN indicator.stoch_values.created_at IS 'Момент вставки значения стохастика.';
CREATE INDEX IF NOT EXISTS ix_stoch_values_params
    ON indicator.stoch_values (indicator_params_id);
CREATE INDEX IF NOT EXISTS ix_stoch_values_candle
    ON indicator.stoch_values (candle_id);

-- Таблица значений Bollinger Bands.
CREATE TABLE IF NOT EXISTS indicator.bb_values
(
    id BIGSERIAL PRIMARY KEY,
    candle_id UUID NOT NULL REFERENCES candles.candle (id),
    indicator_params_id BIGINT NOT NULL REFERENCES indicator.indicator_params (id),
    version INTEGER NOT NULL,
    basis NUMERIC(50, 30) NOT NULL,
    upper NUMERIC(50, 30) NOT NULL,
    lower NUMERIC(50, 30) NOT NULL,
    percent_b NUMERIC(50, 30) NOT NULL,
    bandwidth NUMERIC(50, 30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (candle_id, indicator_params_id, version)
);
COMMENT ON TABLE indicator.bb_values IS 'Значения полос Боллинджера по свечам.';
COMMENT ON COLUMN indicator.bb_values.id IS 'Уникальный идентификатор значения полос Боллинджера.';
COMMENT ON COLUMN indicator.bb_values.candle_id IS 'Свеча, для которой рассчитаны полосы Боллинджера.';
COMMENT ON COLUMN indicator.bb_values.indicator_params_id IS 'Параметры полос Боллинджера.';
COMMENT ON COLUMN indicator.bb_values.version IS 'Версия алгоритма расчёта полос Боллинджера.';
COMMENT ON COLUMN indicator.bb_values.basis IS 'Средняя линия полос Боллинджера.';
COMMENT ON COLUMN indicator.bb_values.upper IS 'Верхняя полоса Боллинджера.';
COMMENT ON COLUMN indicator.bb_values.lower IS 'Нижняя полоса Боллинджера.';
COMMENT ON COLUMN indicator.bb_values.percent_b IS 'Значение %B полос Боллинджера.';
COMMENT ON COLUMN indicator.bb_values.bandwidth IS 'Ширина полос Боллинджера.';
COMMENT ON COLUMN indicator.bb_values.created_at IS 'Момент вставки значения полос Боллинджера.';
CREATE INDEX IF NOT EXISTS ix_bb_values_params
    ON indicator.bb_values (indicator_params_id);
CREATE INDEX IF NOT EXISTS ix_bb_values_candle
    ON indicator.bb_values (candle_id);

-- Таблица значений OBV.
CREATE TABLE IF NOT EXISTS indicator.obv_values
(
    id BIGSERIAL PRIMARY KEY,
    candle_id UUID NOT NULL REFERENCES candles.candle (id),
    indicator_params_id BIGINT NOT NULL REFERENCES indicator.indicator_params (id),
    version INTEGER NOT NULL,
    obv NUMERIC(50, 30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (candle_id, indicator_params_id, version)
);
COMMENT ON TABLE indicator.obv_values IS 'Значения индикатора On-Balance Volume по свечам.';
COMMENT ON COLUMN indicator.obv_values.id IS 'Уникальный идентификатор значения OBV.';
COMMENT ON COLUMN indicator.obv_values.candle_id IS 'Свеча, для которой рассчитан OBV.';
COMMENT ON COLUMN indicator.obv_values.indicator_params_id IS 'Параметры индикатора OBV.';
COMMENT ON COLUMN indicator.obv_values.version IS 'Версия алгоритма расчёта OBV.';
COMMENT ON COLUMN indicator.obv_values.obv IS 'Значение On-Balance Volume.';
COMMENT ON COLUMN indicator.obv_values.created_at IS 'Момент вставки значения OBV.';
CREATE INDEX IF NOT EXISTS ix_obv_values_params
    ON indicator.obv_values (indicator_params_id);
CREATE INDEX IF NOT EXISTS ix_obv_values_candle
    ON indicator.obv_values (candle_id);
