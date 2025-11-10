-- Core schema for trading bot domain
CREATE TABLE exchange (
    id            BIGSERIAL PRIMARY KEY,
    name          TEXT        NOT NULL UNIQUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE instrument (
    id            BIGSERIAL      PRIMARY KEY,
    name          TEXT           NOT NULL UNIQUE,
    base          TEXT           NOT NULL,
    quote         TEXT           NOT NULL,
    price_step    NUMERIC(50,30) NOT NULL,
    qty_step      NUMERIC(50,30) NOT NULL,
    min_notional  NUMERIC(50,30),
    is_perpetual  BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE exchange_instrument (
    id             BIGSERIAL     PRIMARY KEY,
    exchange_id    BIGINT        NOT NULL REFERENCES exchange (id),
    instrument_id  BIGINT        NOT NULL REFERENCES instrument (id),
    client_symbol  TEXT          NOT NULL,
    contract_type  TEXT          NOT NULL DEFAULT 'SWAP',
    margin_mode    TEXT          NOT NULL DEFAULT 'ISOLATED',
    leverage_max   NUMERIC(12,2),
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    UNIQUE (exchange_id, client_symbol),
    UNIQUE (exchange_id, instrument_id)
);

CREATE TABLE history_group (
    id                     BIGSERIAL   PRIMARY KEY,
    exchange_instrument_id BIGINT      NOT NULL REFERENCES exchange_instrument (id),
    timeframe_canonical    TEXT        NOT NULL,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    coverage_start_utc     TIMESTAMPTZ,
    UNIQUE (exchange_instrument_id, timeframe_canonical)
);

CREATE TABLE candles (
    id                 BIGSERIAL      PRIMARY KEY,
    history_group_id   BIGINT         NOT NULL REFERENCES history_group (id) ON DELETE CASCADE,
    timestamp_utc      TIMESTAMPTZ    NOT NULL,
    open_price         NUMERIC(50,30) NOT NULL,
    high_price         NUMERIC(50,30) NOT NULL,
    low_price          NUMERIC(50,30) NOT NULL,
    close_price        NUMERIC(50,30) NOT NULL,
    volume_coin        NUMERIC(50,30),
    volume_currency    NUMERIC(50,30),
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    UNIQUE (history_group_id, timestamp_utc)
);

CREATE TABLE indicator_params (
    id                  BIGSERIAL   PRIMARY KEY,
    type                TEXT        NOT NULL,
    timeframe_canonical TEXT,
    version             TEXT        NOT NULL,
    canonical_json      JSONB       NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          TEXT,
    is_active           BOOLEAN     NOT NULL DEFAULT TRUE,
    UNIQUE (type, timeframe_canonical, version, canonical_json)
);

CREATE TABLE signal_params (
    id                  BIGSERIAL   PRIMARY KEY,
    type                TEXT        NOT NULL,
    timeframe_canonical TEXT,
    version             TEXT        NOT NULL,
    canonical_json      JSONB       NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          TEXT,
    is_active           BOOLEAN     NOT NULL DEFAULT TRUE,
    UNIQUE (type, timeframe_canonical, version, canonical_json)
);

CREATE TABLE quorum_params (
    id             BIGSERIAL   PRIMARY KEY,
    version        TEXT        NOT NULL,
    canonical_json JSONB       NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by     TEXT,
    is_active      BOOLEAN     NOT NULL DEFAULT TRUE,
    UNIQUE (version, canonical_json)
);

CREATE TABLE risk_params (
    id             BIGSERIAL   PRIMARY KEY,
    version        TEXT        NOT NULL,
    canonical_json JSONB       NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by     TEXT,
    is_active      BOOLEAN     NOT NULL DEFAULT TRUE,
    UNIQUE (version, canonical_json)
);

CREATE TABLE exchange_params (
    id             BIGSERIAL   PRIMARY KEY,
    exchange_id    BIGINT      NOT NULL REFERENCES exchange (id),
    version        TEXT        NOT NULL,
    canonical_json JSONB       NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by     TEXT,
    is_active      BOOLEAN     NOT NULL DEFAULT TRUE,
    UNIQUE (exchange_id, version, canonical_json)
);

CREATE TABLE indicator_checkpoint (
    id                  BIGSERIAL   PRIMARY KEY,
    indicator           TEXT        NOT NULL,
    history_group_id    BIGINT      NOT NULL REFERENCES history_group (id),
    indicator_params_id BIGINT      NOT NULL REFERENCES indicator_params (id),
    version             TEXT        NOT NULL,
    last_ts             TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (indicator, history_group_id, indicator_params_id, version)
);

CREATE TABLE exchange_instrument_coverage (
    exchange_instrument_id BIGINT      PRIMARY KEY REFERENCES exchange_instrument (id) ON DELETE CASCADE,
    coverage_start_utc     TIMESTAMPTZ NOT NULL,
    coverage_end_utc       TIMESTAMPTZ,
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
