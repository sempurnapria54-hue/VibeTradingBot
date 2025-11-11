-- Создание расширений, необходимых для UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Создание схем по доменам данных
CREATE SCHEMA IF NOT EXISTS exchange;
COMMENT ON SCHEMA exchange IS 'Схема справочника бирж.';

CREATE SCHEMA IF NOT EXISTS instrument;
COMMENT ON SCHEMA instrument IS 'Схема справочника инструментов.';

CREATE SCHEMA IF NOT EXISTS exchange_instrument;
COMMENT ON SCHEMA exchange_instrument IS 'Схема связей бирж и инструментов.';

CREATE SCHEMA IF NOT EXISTS candles;
COMMENT ON SCHEMA candles IS 'Схема хранения свечей.';

CREATE SCHEMA IF NOT EXISTS coverage;
COMMENT ON SCHEMA coverage IS 'Схема хранения покрытия данных по свечам.';

CREATE SCHEMA IF NOT EXISTS params;
COMMENT ON SCHEMA params IS 'Схема хранения параметров конфигурации.';

-- Таблица бирж
CREATE TABLE IF NOT EXISTS exchange.exchange
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(32)      NOT NULL,
    name        VARCHAR(128)     NOT NULL,
    status      VARCHAR(32)      NOT NULL,
    created_at  TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE exchange.exchange IS 'Справочник доступных бирж.';
COMMENT ON COLUMN exchange.exchange.id IS 'Уникальный идентификатор биржи.';
COMMENT ON COLUMN exchange.exchange.code IS 'Канонический код биржи.';
COMMENT ON COLUMN exchange.exchange.name IS 'Человекочитаемое название биржи.';
COMMENT ON COLUMN exchange.exchange.status IS 'Текущий статус биржи.';
COMMENT ON COLUMN exchange.exchange.created_at IS 'Момент создания записи.';
COMMENT ON COLUMN exchange.exchange.updated_at IS 'Момент последнего обновления записи.';

CREATE UNIQUE INDEX IF NOT EXISTS ux_exchange_code ON exchange.exchange (code);

-- Тип и таблица инструментов
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'instrument_type_enum') THEN
        CREATE TYPE instrument.instrument_type_enum AS ENUM ('CRYPTO_SWAP', 'CRYPTO_SPOT', 'CRYPTO_FUTURES');
    END IF;
END
$$;

CREATE TABLE IF NOT EXISTS instrument.instrument
(
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol                VARCHAR(64)                           NOT NULL,
    base_currency         VARCHAR(16)                           NOT NULL,
    quote_currency        VARCHAR(16)                           NOT NULL,
    instrument_type       instrument.instrument_type_enum       NOT NULL,
    price_precision       INTEGER                               NOT NULL,
    quantity_precision    INTEGER                               NOT NULL,
    created_at            TIMESTAMPTZ                           NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ                           NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE instrument.instrument IS 'Справочник доменных инструментов.';
COMMENT ON COLUMN instrument.instrument.id IS 'Уникальный идентификатор инструмента.';
COMMENT ON COLUMN instrument.instrument.symbol IS 'Канонический символ инструмента.';
COMMENT ON COLUMN instrument.instrument.base_currency IS 'Базовая валюта инструмента.';
COMMENT ON COLUMN instrument.instrument.quote_currency IS 'Котируемая валюта инструмента.';
COMMENT ON COLUMN instrument.instrument.instrument_type IS 'Тип инструмента.';
COMMENT ON COLUMN instrument.instrument.price_precision IS 'Количество знаков после запятой для цены.';
COMMENT ON COLUMN instrument.instrument.quantity_precision IS 'Количество знаков после запятой для количества.';
COMMENT ON COLUMN instrument.instrument.created_at IS 'Момент создания записи.';
COMMENT ON COLUMN instrument.instrument.updated_at IS 'Момент последнего обновления записи.';

CREATE UNIQUE INDEX IF NOT EXISTS ux_instrument_symbol ON instrument.instrument (symbol);

-- Таблица связей бирж и инструментов
CREATE TABLE IF NOT EXISTS exchange_instrument.exchange_instrument
(
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exchange_id            UUID            NOT NULL REFERENCES exchange.exchange (id),
    instrument_id          UUID            NOT NULL REFERENCES instrument.instrument (id),
    exchange_symbol        VARCHAR(128)    NOT NULL,
    min_trade_quantity     NUMERIC(50, 30) NOT NULL,
    tick_size              NUMERIC(50, 30) NOT NULL,
    maker_fee_rate         NUMERIC(50, 30) NOT NULL,
    taker_fee_rate         NUMERIC(50, 30) NOT NULL,
    is_active              BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at             TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE exchange_instrument.exchange_instrument IS 'Связь биржи и инструмента с параметрами торговли.';
COMMENT ON COLUMN exchange_instrument.exchange_instrument.id IS 'Уникальный идентификатор связи биржи и инструмента.';
COMMENT ON COLUMN exchange_instrument.exchange_instrument.exchange_id IS 'Ссылка на биржу.';
COMMENT ON COLUMN exchange_instrument.exchange_instrument.instrument_id IS 'Ссылка на инструмент.';
COMMENT ON COLUMN exchange_instrument.exchange_instrument.exchange_symbol IS 'Символ на стороне биржи.';
COMMENT ON COLUMN exchange_instrument.exchange_instrument.min_trade_quantity IS 'Минимально допустимое количество для сделки.';
COMMENT ON COLUMN exchange_instrument.exchange_instrument.tick_size IS 'Минимальный шаг цены.';
COMMENT ON COLUMN exchange_instrument.exchange_instrument.maker_fee_rate IS 'Комиссия мейкера.';
COMMENT ON COLUMN exchange_instrument.exchange_instrument.taker_fee_rate IS 'Комиссия тейкера.';
COMMENT ON COLUMN exchange_instrument.exchange_instrument.is_active IS 'Флаг активности связки.';
COMMENT ON COLUMN exchange_instrument.exchange_instrument.created_at IS 'Момент создания записи.';
COMMENT ON COLUMN exchange_instrument.exchange_instrument.updated_at IS 'Момент последнего обновления записи.';

CREATE UNIQUE INDEX IF NOT EXISTS ux_exchange_instrument_pair
    ON exchange_instrument.exchange_instrument (exchange_id, instrument_id);

-- Таблица свечей
CREATE TABLE IF NOT EXISTS candles.candle
(
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    exchange_instrument_id  UUID            NOT NULL REFERENCES exchange_instrument.exchange_instrument (id),
    timeframe               VARCHAR(32)     NOT NULL,
    open_time_utc           TIMESTAMPTZ     NOT NULL,
    close_time_utc          TIMESTAMPTZ     NOT NULL,
    open_price              NUMERIC(50, 30) NOT NULL,
    close_price             NUMERIC(50, 30) NOT NULL,
    high_price              NUMERIC(50, 30) NOT NULL,
    low_price               NUMERIC(50, 30) NOT NULL,
    volume                  NUMERIC(50, 30) NOT NULL,
    trades_count            BIGINT          NOT NULL,
    coverage_end_utc        TIMESTAMPTZ     NOT NULL,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE candles.candle IS 'Хранилище нормализованных свечей.';
COMMENT ON COLUMN candles.candle.id IS 'Уникальный идентификатор свечи.';
COMMENT ON COLUMN candles.candle.exchange_instrument_id IS 'Связка биржи и инструмента, которой принадлежит свеча.';
COMMENT ON COLUMN candles.candle.timeframe IS 'Канонический таймфрейм свечи.';
COMMENT ON COLUMN candles.candle.open_time_utc IS 'Время открытия свечи в UTC.';
COMMENT ON COLUMN candles.candle.close_time_utc IS 'Время закрытия свечи в UTC.';
COMMENT ON COLUMN candles.candle.open_price IS 'Цена открытия свечи.';
COMMENT ON COLUMN candles.candle.close_price IS 'Цена закрытия свечи.';
COMMENT ON COLUMN candles.candle.high_price IS 'Максимальная цена свечи.';
COMMENT ON COLUMN candles.candle.low_price IS 'Минимальная цена свечи.';
COMMENT ON COLUMN candles.candle.volume IS 'Объем торгов по свечe.';
COMMENT ON COLUMN candles.candle.trades_count IS 'Количество сделок внутри свечи.';
COMMENT ON COLUMN candles.candle.coverage_end_utc IS 'Момент, до которого подтверждено покрытие данных.';
COMMENT ON COLUMN candles.candle.created_at IS 'Момент загрузки свечи.';

CREATE UNIQUE INDEX IF NOT EXISTS ux_candle_unique
    ON candles.candle (exchange_instrument_id, timeframe, open_time_utc);
CREATE INDEX IF NOT EXISTS ix_candle_exchange_instrument ON candles.candle (exchange_instrument_id);
CREATE INDEX IF NOT EXISTS ix_candle_timeframe ON candles.candle (timeframe);
CREATE INDEX IF NOT EXISTS ix_candle_open_time ON candles.candle (open_time_utc);

-- Таблица покрытия
CREATE TABLE IF NOT EXISTS coverage.candle_coverage
(
    id                     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    exchange_instrument_id UUID        NOT NULL REFERENCES exchange_instrument.exchange_instrument (id),
    timeframe              VARCHAR(32) NOT NULL,
    coverage_start_utc     TIMESTAMPTZ NOT NULL,
    coverage_end_utc       TIMESTAMPTZ NOT NULL,
    is_complete            BOOLEAN     NOT NULL DEFAULT FALSE,
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE coverage.candle_coverage IS 'Реестр покрытия исторических данных по свечам.';
COMMENT ON COLUMN coverage.candle_coverage.id IS 'Уникальный идентификатор записи покрытия.';
COMMENT ON COLUMN coverage.candle_coverage.exchange_instrument_id IS 'Связка биржи и инструмента для покрытия.';
COMMENT ON COLUMN coverage.candle_coverage.timeframe IS 'Канонический таймфрейм покрытия.';
COMMENT ON COLUMN coverage.candle_coverage.coverage_start_utc IS 'Начало диапазона покрытия в UTC.';
COMMENT ON COLUMN coverage.candle_coverage.coverage_end_utc IS 'Конец диапазона покрытия в UTC.';
COMMENT ON COLUMN coverage.candle_coverage.is_complete IS 'Флаг завершенности покрытия.';
COMMENT ON COLUMN coverage.candle_coverage.updated_at IS 'Момент последнего обновления записи покрытия.';

CREATE UNIQUE INDEX IF NOT EXISTS ux_candle_coverage_unique
    ON coverage.candle_coverage (exchange_instrument_id, timeframe);
CREATE INDEX IF NOT EXISTS ix_candle_coverage_exchange_instrument
    ON coverage.candle_coverage (exchange_instrument_id);

-- Таблицы параметров
CREATE TABLE IF NOT EXISTS params.exchange_params
(
    id              BIGSERIAL PRIMARY KEY,
    exchange_id     UUID        NOT NULL REFERENCES exchange.exchange (id),
    version         INTEGER     NOT NULL,
    canonical_json  JSONB       NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE params.exchange_params IS 'Версионированные параметры бирж.';
COMMENT ON COLUMN params.exchange_params.id IS 'Уникальный идентификатор записи параметров биржи.';
COMMENT ON COLUMN params.exchange_params.exchange_id IS 'Ссылка на биржу, к которой относятся параметры.';
COMMENT ON COLUMN params.exchange_params.version IS 'Версия параметров биржи.';
COMMENT ON COLUMN params.exchange_params.canonical_json IS 'Канонический JSON параметров биржи.';
COMMENT ON COLUMN params.exchange_params.created_at IS 'Момент фиксации версии параметров биржи.';

CREATE UNIQUE INDEX IF NOT EXISTS ux_exchange_params_version
    ON params.exchange_params (exchange_id, version);

CREATE TABLE IF NOT EXISTS params.instrument_params
(
    id              BIGSERIAL PRIMARY KEY,
    instrument_id   UUID        NOT NULL REFERENCES instrument.instrument (id),
    timeframe       VARCHAR(32) NOT NULL,
    version         INTEGER     NOT NULL,
    canonical_json  JSONB       NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE params.instrument_params IS 'Версионированные параметры инструментов по таймфреймам.';
COMMENT ON COLUMN params.instrument_params.id IS 'Уникальный идентификатор записи параметров инструмента.';
COMMENT ON COLUMN params.instrument_params.instrument_id IS 'Ссылка на инструмент, к которому относятся параметры.';
COMMENT ON COLUMN params.instrument_params.timeframe IS 'Канонический таймфрейм параметров.';
COMMENT ON COLUMN params.instrument_params.version IS 'Версия параметров инструмента.';
COMMENT ON COLUMN params.instrument_params.canonical_json IS 'Канонический JSON параметров инструмента.';
COMMENT ON COLUMN params.instrument_params.created_at IS 'Момент фиксации параметров инструмента.';

CREATE UNIQUE INDEX IF NOT EXISTS ux_instrument_params_version
    ON params.instrument_params (instrument_id, timeframe, version);
