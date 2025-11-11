-- Схема и таблицы для кворумного движка и решений.
CREATE SCHEMA IF NOT EXISTS quorum;
COMMENT ON SCHEMA quorum IS 'События кворума, решения и чекпоинты агрегатора.';

-- Реестр параметров кворума (append-only).
CREATE TABLE IF NOT EXISTS params.quorum_params
(
    id BIGSERIAL PRIMARY KEY,
    timeframe_canonical TEXT NOT NULL,
    version TEXT NOT NULL,
    canonical_json JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by TEXT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);
COMMENT ON TABLE params.quorum_params IS 'Версии параметров кворума для агрегатора решений.';
COMMENT ON COLUMN params.quorum_params.id IS 'Уникальный идентификатор профиля параметров кворума.';
COMMENT ON COLUMN params.quorum_params.timeframe_canonical IS 'Канонический таймфрейм, для которого актуальны параметры.';
COMMENT ON COLUMN params.quorum_params.version IS 'Версия правил кворума.';
COMMENT ON COLUMN params.quorum_params.canonical_json IS 'Канонизированный JSON конфигурации кворума (веса, фильтры, пороги).';
COMMENT ON COLUMN params.quorum_params.created_at IS 'Момент фиксации параметров кворума (UTC).';
COMMENT ON COLUMN params.quorum_params.created_by IS 'Автор записи параметров кворума.';
COMMENT ON COLUMN params.quorum_params.is_active IS 'Флаг актуальности параметров кворума.';

CREATE UNIQUE INDEX IF NOT EXISTS ux_quorum_params_unique
    ON params.quorum_params (timeframe_canonical, version, canonical_json);
CREATE INDEX IF NOT EXISTS ix_quorum_params_version
    ON params.quorum_params (version);

-- Журнал решений кворума.
CREATE TABLE IF NOT EXISTS quorum.quorum_decision_event
(
    id BIGSERIAL PRIMARY KEY,
    exchange_instrument_id UUID NOT NULL REFERENCES exchange_instrument.exchange_instrument (id),
    timeframe_canonical TEXT NOT NULL,
    ts_utc TIMESTAMPTZ NOT NULL,
    action TEXT NOT NULL,
    direction TEXT NULL,
    score NUMERIC(10, 6) NOT NULL,
    threshold_enter NUMERIC(10, 6) NOT NULL,
    threshold_exit NUMERIC(10, 6) NOT NULL,
    quorum_params_id BIGINT NOT NULL REFERENCES params.quorum_params (id),
    version INTEGER NOT NULL,
    position_state JSONB NOT NULL,
    reason JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (exchange_instrument_id, timeframe_canonical, ts_utc, quorum_params_id, version)
);
COMMENT ON TABLE quorum.quorum_decision_event IS 'Журнал агрегированных решений кворума по сериям.';
COMMENT ON COLUMN quorum.quorum_decision_event.id IS 'Уникальный идентификатор события решения кворума.';
COMMENT ON COLUMN quorum.quorum_decision_event.exchange_instrument_id IS 'Связка биржи и инструмента, для которой принято решение.';
COMMENT ON COLUMN quorum.quorum_decision_event.timeframe_canonical IS 'Канонический таймфрейм, на котором считался кворум.';
COMMENT ON COLUMN quorum.quorum_decision_event.ts_utc IS 'Время (UTC) закрытия бара, на котором принято решение.';
COMMENT ON COLUMN quorum.quorum_decision_event.action IS 'Решение кворума (ENTER, EXIT, REVERSE, MODIFY_SLTP, HOLD).';
COMMENT ON COLUMN quorum.quorum_decision_event.direction IS 'Направление позиции (LONG/SHORT) при действии ENTER или REVERSE.';
COMMENT ON COLUMN quorum.quorum_decision_event.score IS 'Итоговый скор кворума после фильтров.';
COMMENT ON COLUMN quorum.quorum_decision_event.threshold_enter IS 'Порог входа, применённый при расчёте решения.';
COMMENT ON COLUMN quorum.quorum_decision_event.threshold_exit IS 'Порог выхода, применённый при расчёте решения.';
COMMENT ON COLUMN quorum.quorum_decision_event.quorum_params_id IS 'Ссылка на параметры кворума (immutable запись).';
COMMENT ON COLUMN quorum.quorum_decision_event.version IS 'Версия алгоритма кворума.';
COMMENT ON COLUMN quorum.quorum_decision_event.position_state IS 'Снимок состояния позиции на момент решения (JSONB).';
COMMENT ON COLUMN quorum.quorum_decision_event.reason IS 'Структурированная объяснимость решения кворума (JSONB).';
COMMENT ON COLUMN quorum.quorum_decision_event.created_at IS 'Момент вставки решения в журнал (UTC).';

CREATE INDEX IF NOT EXISTS ix_quorum_decision_series
    ON quorum.quorum_decision_event (exchange_instrument_id, timeframe_canonical, ts_utc);
CREATE INDEX IF NOT EXISTS ix_quorum_decision_action
    ON quorum.quorum_decision_event (action);

-- Чекпоинты кворума по сериям.
CREATE TABLE IF NOT EXISTS quorum.quorum_checkpoint
(
    quorum_params_id BIGINT NOT NULL REFERENCES params.quorum_params (id),
    version INTEGER NOT NULL,
    exchange_instrument_id UUID NOT NULL REFERENCES exchange_instrument.exchange_instrument (id),
    timeframe_canonical TEXT NOT NULL,
    last_ts TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (quorum_params_id, version, exchange_instrument_id, timeframe_canonical)
);
COMMENT ON TABLE quorum.quorum_checkpoint IS 'Чекпоинты прохождения кворума по сериям таймфреймов.';
COMMENT ON COLUMN quorum.quorum_checkpoint.quorum_params_id IS 'Параметры кворума, по которым ведётся прогон.';
COMMENT ON COLUMN quorum.quorum_checkpoint.version IS 'Версия алгоритма кворума.';
COMMENT ON COLUMN quorum.quorum_checkpoint.exchange_instrument_id IS 'Связка биржи и инструмента серии кворума.';
COMMENT ON COLUMN quorum.quorum_checkpoint.timeframe_canonical IS 'Канонический таймфрейм, по которому ведётся кворум.';
COMMENT ON COLUMN quorum.quorum_checkpoint.last_ts IS 'Последнее обработанное время свечи (UTC).';
COMMENT ON COLUMN quorum.quorum_checkpoint.updated_at IS 'Момент обновления чекпоинта (UTC).';

CREATE INDEX IF NOT EXISTS ix_quorum_checkpoint_series
    ON quorum.quorum_checkpoint (exchange_instrument_id, timeframe_canonical);
