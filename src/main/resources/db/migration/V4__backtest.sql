-- Схема и таблицы для оффлайн-бэктестов.
CREATE SCHEMA IF NOT EXISTS backtest;
COMMENT ON SCHEMA backtest IS 'Снимки конфигураций, сделки и метрики запусков бэктеста.';

-- Таблица запусков бэктеста.
CREATE TABLE IF NOT EXISTS backtest.backtest_run
(
    id BIGSERIAL PRIMARY KEY,
    exchange_instrument_id UUID NOT NULL REFERENCES exchange_instrument.exchange_instrument (id),
    timeframe_canonical TEXT NOT NULL,
    from_utc TIMESTAMPTZ NOT NULL,
    to_utc TIMESTAMPTZ NOT NULL,
    signal_types TEXT[] NOT NULL,
    signal_params_id BIGINT NOT NULL,
    quorum_params_id BIGINT NULL,
    risk_params_id BIGINT NOT NULL,
    exchange_params_id BIGINT NOT NULL,
    backtest_params_id BIGINT NOT NULL,
    params_json JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (backtest_params_id, exchange_instrument_id, timeframe_canonical, from_utc, to_utc)
);
COMMENT ON TABLE backtest.backtest_run IS 'Запуски оффлайн-бэктеста с фиксированными конфигурациями.';
COMMENT ON COLUMN backtest.backtest_run.id IS 'Уникальный идентификатор запуска бэктеста.';
COMMENT ON COLUMN backtest.backtest_run.exchange_instrument_id IS 'Связка биржи и инструмента для запуска.';
COMMENT ON COLUMN backtest.backtest_run.timeframe_canonical IS 'Канонический таймфрейм свечей (UTC).';
COMMENT ON COLUMN backtest.backtest_run.from_utc IS 'Начало интервала симуляции (UTC).';
COMMENT ON COLUMN backtest.backtest_run.to_utc IS 'Конец интервала симуляции (UTC).';
COMMENT ON COLUMN backtest.backtest_run.signal_types IS 'Типы сигналов, участвующие в симуляции.';
COMMENT ON COLUMN backtest.backtest_run.signal_params_id IS 'Ссылка на параметры сигналов (immutable).';
COMMENT ON COLUMN backtest.backtest_run.quorum_params_id IS 'Параметры агрегирующего скоринга (опционально).';
COMMENT ON COLUMN backtest.backtest_run.risk_params_id IS 'Параметры риск-модели.';
COMMENT ON COLUMN backtest.backtest_run.exchange_params_id IS 'Параметры комиссий/ограничений биржи.';
COMMENT ON COLUMN backtest.backtest_run.backtest_params_id IS 'Параметры модели бэктеста (latency, slippage и т.д.).';
COMMENT ON COLUMN backtest.backtest_run.params_json IS 'Канонический JSON снимка конфигурации запуска.';
COMMENT ON COLUMN backtest.backtest_run.created_at IS 'Момент фиксации запуска (UTC).';

-- Таблица сделок бэктеста.
CREATE TABLE IF NOT EXISTS backtest.backtest_trade
(
    id BIGSERIAL PRIMARY KEY,
    run_id BIGINT NOT NULL REFERENCES backtest.backtest_run (id) ON DELETE CASCADE,
    signal_id BIGINT NULL,
    entry_ts TIMESTAMPTZ NOT NULL,
    exit_ts TIMESTAMPTZ NOT NULL,
    side TEXT NOT NULL,
    entry_price NUMERIC(50, 12) NOT NULL,
    exit_price NUMERIC(50, 12) NOT NULL,
    pnl_r NUMERIC(20, 10) NOT NULL,
    fees NUMERIC(20, 10) NOT NULL,
    slippage NUMERIC(20, 10) NOT NULL,
    funding NUMERIC(20, 10) NOT NULL,
    mae_r NUMERIC(20, 10) NOT NULL,
    mfe_r NUMERIC(20, 10) NOT NULL,
    which_hit_first TEXT NOT NULL,
    time_to_event_bars INT NOT NULL,
    reason JSONB NOT NULL
);
COMMENT ON TABLE backtest.backtest_trade IS 'Детальные сделки бэктеста с метриками исполнения.';
COMMENT ON COLUMN backtest.backtest_trade.id IS 'Уникальный идентификатор сделки.';
COMMENT ON COLUMN backtest.backtest_trade.run_id IS 'Ссылка на запуск бэктеста.';
COMMENT ON COLUMN backtest.backtest_trade.signal_id IS 'Связанный сигнал, инициировавший вход.';
COMMENT ON COLUMN backtest.backtest_trade.entry_ts IS 'Время открытия позиции (UTC).';
COMMENT ON COLUMN backtest.backtest_trade.exit_ts IS 'Время закрытия позиции (UTC).';
COMMENT ON COLUMN backtest.backtest_trade.side IS 'Сторона сделки (LONG/SHORT).';
COMMENT ON COLUMN backtest.backtest_trade.entry_price IS 'Цена входа.';
COMMENT ON COLUMN backtest.backtest_trade.exit_price IS 'Цена выхода.';
COMMENT ON COLUMN backtest.backtest_trade.pnl_r IS 'Результат сделки в R.';
COMMENT ON COLUMN backtest.backtest_trade.fees IS 'Совокупные комиссии.';
COMMENT ON COLUMN backtest.backtest_trade.slippage IS 'Проскальзывание.';
COMMENT ON COLUMN backtest.backtest_trade.funding IS 'Фандинг за период удержания позиции.';
COMMENT ON COLUMN backtest.backtest_trade.mae_r IS 'Максимальное неблагоприятное движение в R.';
COMMENT ON COLUMN backtest.backtest_trade.mfe_r IS 'Максимальное благоприятное движение в R.';
COMMENT ON COLUMN backtest.backtest_trade.which_hit_first IS 'Что было достигнуто первым (TP/SL/TIME/EXIT_RULE).';
COMMENT ON COLUMN backtest.backtest_trade.time_to_event_bars IS 'Количество баров до фиксации результата.';
COMMENT ON COLUMN backtest.backtest_trade.reason IS 'JSON-трасса принятия решения и сопровождения сделки.';
CREATE INDEX IF NOT EXISTS ix_backtest_trade_run ON backtest.backtest_trade (run_id);
CREATE INDEX IF NOT EXISTS ix_backtest_trade_entry_ts ON backtest.backtest_trade (entry_ts);
CREATE INDEX IF NOT EXISTS ix_backtest_trade_signal ON backtest.backtest_trade (signal_id);

-- Кривая капитала.
CREATE TABLE IF NOT EXISTS backtest.backtest_equity_point
(
    id BIGSERIAL PRIMARY KEY,
    run_id BIGINT NOT NULL REFERENCES backtest.backtest_run (id) ON DELETE CASCADE,
    ts_utc TIMESTAMPTZ NOT NULL,
    equity_r NUMERIC(20, 10) NOT NULL
);
COMMENT ON TABLE backtest.backtest_equity_point IS 'Кривая капитала (в R) для запуска бэктеста.';
COMMENT ON COLUMN backtest.backtest_equity_point.id IS 'Уникальный идентификатор точки кривой капитала.';
COMMENT ON COLUMN backtest.backtest_equity_point.run_id IS 'Запуск бэктеста, которому принадлежит точка.';
COMMENT ON COLUMN backtest.backtest_equity_point.ts_utc IS 'Время фиксации значения equity (UTC).';
COMMENT ON COLUMN backtest.backtest_equity_point.equity_r IS 'Значение equity в R.';
CREATE INDEX IF NOT EXISTS ix_backtest_equity_run ON backtest.backtest_equity_point (run_id, ts_utc);

-- Агрегированные метрики.
CREATE TABLE IF NOT EXISTS backtest.performance_aggregate
(
    id BIGSERIAL PRIMARY KEY,
    run_id BIGINT NOT NULL REFERENCES backtest.backtest_run (id) ON DELETE CASCADE,
    trades INT NOT NULL,
    winrate NUMERIC(6, 4) NOT NULL,
    profit_factor NUMERIC(12, 6) NOT NULL,
    expectancy_r NUMERIC(12, 6) NOT NULL,
    max_drawdown_r NUMERIC(12, 6) NOT NULL,
    sharpe NUMERIC(12, 6) NULL,
    sortino NUMERIC(12, 6) NULL
);
COMMENT ON TABLE backtest.performance_aggregate IS 'Агрегированные показатели выполнения бэктеста.';
COMMENT ON COLUMN backtest.performance_aggregate.id IS 'Уникальный идентификатор агрегированных метрик.';
COMMENT ON COLUMN backtest.performance_aggregate.run_id IS 'Запуск бэктеста.';
COMMENT ON COLUMN backtest.performance_aggregate.trades IS 'Количество сделок.';
COMMENT ON COLUMN backtest.performance_aggregate.winrate IS 'Доля прибыльных сделок.';
COMMENT ON COLUMN backtest.performance_aggregate.profit_factor IS 'Profit factor запуска.';
COMMENT ON COLUMN backtest.performance_aggregate.expectancy_r IS 'Ожидаемое значение сделки (в R).';
COMMENT ON COLUMN backtest.performance_aggregate.max_drawdown_r IS 'Максимальная просадка в R.';
COMMENT ON COLUMN backtest.performance_aggregate.sharpe IS 'Коэффициент Шарпа (опционально).';
COMMENT ON COLUMN backtest.performance_aggregate.sortino IS 'Коэффициент Сортино (опционально).';
CREATE UNIQUE INDEX IF NOT EXISTS ux_backtest_performance_run ON backtest.performance_aggregate (run_id);
