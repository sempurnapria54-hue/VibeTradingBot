# M2_SCHEMA_AND_MIGRATIONS.md — ядро БД (канонические таймфреймы, серия = exchange_instrument×TF)

Версия: **2.2**
Ключи: канонические таймфреймы в YAML; свечные ряды — на уровне `exchange_instrument`; никакого статуса свечей; `*_params` — append-only; без триггеров и INSERT в миграциях.

---

## 0) Договорённости

* PostgreSQL 15+, UTC, `TIMESTAMPTZ`, числа `NUMERIC(50,30)`.
* Канонические ТФ — `TEXT` (напр. "1 hour", "1 day").
* Маппинг каноника→биржа — в `application.yml`:

  ```yaml
  timeframes:
    canonical: ["1 minute","3 minutes","5 minutes","15 minutes","1 hour","2 hours","4 hours","1 day"]
    exchanges:
      okx: {"1 minute":"1m","3 minutes":"3m","5 minutes":"5m","15 minutes":"15m","1 hour":"1H","2 hours":"2H","4 hours":"4H","1 day":"1D"}
      binance: {"1 minute":"1m","1 hour":"1h","1 day":"1d"}
  ```
* Иммутабельность `*_params` — роли БД (SELECT/INSERT) + DAO (`resolveOrCreate`).
* **Без триггеров**, **без статуса свечей**, **без INSERT** в миграциях.

---

## 1) ER-обзор (словами)

* `exchange` — биржи.
* `instrument` — доменные инструменты (ETH-USDT и т.п.).
* `exchange_instrument` — **реальная торговая связка** «биржа×инструмент» (там же биржевой символ, маржа, плечо).
* `history_group` — **серия** свечей для `exchange_instrument` на конкретном **каноническом ТФ**.
* `candles` — OHLCV, принадлежат `history_group`.
* `*_params` — реестры параметров (append-only).
* `indicator_checkpoint` — прогресс расчёта индикаторов **по серии** (`history_group`).
* `exchange_instrument_coverage` — окно «полного покрытия» истории **по связке** (когда все нужные ТФ закрыты): хранит `coverage_start_utc` и `coverage_end_utc`.

---

## 2) Таблицы

### 2.1 `exchange` — биржи

**Назначение:** справочник поддерживаемых бирж. Используется для связи с `exchange_instrument` и параметрами биржи.

| Колонка    | Тип         | NULL | Default | Комментарий                                                       |
| ---------- | ----------- | ---- | ------- | ----------------------------------------------------------------- |
| id         | BIGSERIAL   | NO   | —       | PK, идентификатор биржи.                                          |
| name       | TEXT        | NO   | —       | **Уникальное** человекочитаемое имя (например: `okx`, `binance`). |
| created_at | TIMESTAMPTZ | NO   | NOW()   | Момент создания записи (UTC).                                     |

**Ограничения и индексы**

* `UNIQUE (name)` — идемпотентное создание биржи.
* Индекс по PK создаётся автоматически.

**Примечания по использованию**

* Записи в этой таблице редкие, изменяемые. Удаление следует производить только при отсутствии зависимостей (см. FK из `exchange_instrument`, `exchange_params`).

---

### 2.2 `instrument` — доменные инструменты

**Назначение:** единый доменный инструмент (например, `ETH-USDT`), независимый от формата конкретной биржи.

| Колонка      | Тип            | NULL | Default | Комментарий                                                                     |
| ------------ | -------------- | ---- | ------- | ------------------------------------------------------------------------------- |
| id           | BIGSERIAL      | NO   | —       | PK.                                                                             |
| name         | TEXT           | NO   | —       | **Уникальное** имя в доменной нотации (например: `ETH-USDT`).                   |
| base         | TEXT           | NO   | —       | Базовый актив (пример: `ETH`).                                                  |
| quote        | TEXT           | NO   | —       | Котируемый актив (пример: `USDT`).                                              |
| price_step   | NUMERIC(50,30) | NO   | —       | Минимальный шаг цены для округления заявок.                                     |
| qty_step     | NUMERIC(50,30) | NO   | —       | Минимальный шаг количества (лота).                                              |
| min_notional | NUMERIC(50,30) | YES  | —       | Минимальный номинал сделки в валюте котировки (если применимо).                 |
| is_perpetual | BOOLEAN        | NO   | TRUE    | Флаг бессрочного контракта (`SWAP`). Для спотовых/фьючерсов может быть `FALSE`. |
| created_at   | TIMESTAMPTZ    | NO   | NOW()   | Создание записи (UTC).                                                          |

**Ограничения и индексы**

* `UNIQUE (name)` — идемпотентность доменного инструмента.
* Не содержит прямой связи с сериями свечей; конкретные ряды ведутся на уровне `exchange_instrument`.

**Примечания по использованию**

* Доменные параметры (`price_step`, `qty_step`) — «эталонные». Фактические биржевые ограничения уточняются в `exchange_instrument` или через конфиг биржи.

---

### 2.3 `exchange_instrument`

Связка «биржа×инструмент».

| Колонка       | Тип           | NULL | Описание                                |
| ------------- | ------------- | ---- | --------------------------------------- |
| id            | BIGSERIAL     | NO   | PK                                      |
| exchange_id   | BIGINT        | NO   | FK→`exchange.id`                        |
| instrument_id | BIGINT        | NO   | FK→`instrument.id`                      |
| client_symbol | TEXT          | NO   | Биржевой символ (напр. `ETH-USDT-SWAP`) |
| contract_type | TEXT          | NO   | `SWAP`/`FUTURE`/`SPOT` (def `SWAP`)     |
| margin_mode   | TEXT          | NO   | `ISOLATED`/`CROSSED` (def `ISOLATED`)   |
| leverage_max  | NUMERIC(12,2) | YES  | Макс. плечо                             |
| created_at    | TIMESTAMPTZ   | NO   | UTC                                     |

**UNIQUE:** `(exchange_id, client_symbol)`, `(exchange_id, instrument_id)`.

### 2.4 `history_group` — серия (exchange_instrument × timeframe_canonical)

| Колонка                | Тип         | NULL | Описание                                       |
| ---------------------- | ----------- | ---- | ---------------------------------------------- |
| id                     | BIGSERIAL   | NO   | PK                                             |
| exchange_instrument_id | BIGINT      | NO   | FK→`exchange_instrument.id`                    |
| timeframe_canonical    | TEXT        | NO   | Канонический ТФ (напр. "1 hour")               |
| created_at             | TIMESTAMPTZ | NO   | UTC                                            |
| coverage_start_utc     | TIMESTAMPTZ | YES  | **Первая полная точка покрытия по этой серии** |

**UNIQUE:** `(exchange_instrument_id, timeframe_canonical)`.

### 2.5 `candles`

| Колонка                     | Тип            | NULL | Описание                                    |
| --------------------------- | -------------- | ---- | ------------------------------------------- |
| id                          | BIGSERIAL      | NO   | PK                                          |
| history_group_id            | BIGINT         | NO   | FK→`history_group.id` **ON DELETE CASCADE** |
| timestamp_utc               | TIMESTAMPTZ    | NO   | Начало свечи UTC                            |
| open/high/low/close         | NUMERIC(50,30) | NO   | OHLC                                        |
| volume_coin/volume_currency | NUMERIC(50,30) | YES  | Объёмы                                      |
| created_at                  | TIMESTAMPTZ    | NO   | UTC                                         |

**UNIQUE:** `(history_group_id, timestamp_utc)`.

### 2.6 `indicator_params` — реестр параметров индикатора (append-only)

**Назначение:** версионное, неизменяемое хранилище конфигураций индикаторов для воспроизводимости расчётов.

| Колонка             | Тип         | NULL | Default | Комментарий                                                                                 |
| ------------------- | ----------- | ---- | ------- | ------------------------------------------------------------------------------------------- |
| id                  | BIGSERIAL   | NO   | —       | PK.                                                                                         |
| type                | TEXT        | NO   | —       | Тип индикатора (например: `EMA`, `RSI`, `MACD`, `BB`, `STOCH`, `OBV`).                      |
| timeframe_canonical | TEXT        | YES  | —       | Канонический ТФ (например: `"1 hour"`). Может быть `NULL`, если конфигурация ТФ-независима. |
| version             | TEXT        | NO   | —       | Версия конфигурации/алгоритма (строка, например: `v1`, `2025-10-08`).                       |
| canonical_json      | JSONB       | NO   | —       | **Канонизированный снимок** параметров (ключи отсортированы, значения нормализованы).       |
| created_at          | TIMESTAMPTZ | NO   | NOW()   | Время вставки (UTC).                                                                        |
| created_by          | TEXT        | YES  | —       | Автор/сервис, создавший запись.                                                             |
| is_active           | BOOLEAN     | NO   | TRUE    | Флаг активности (для выбора по умолчанию).                                                  |

**Ограничения и индексы**

* `UNIQUE (type, timeframe_canonical, version, canonical_json)` — главная гарантия идемпотентности.
* Рекомендуемые индексы для быстрых поисков:

    * `BTREE (type, timeframe_canonical, version)` — выбор активной/последней конфигурации.

**Пример `canonical_json`** (EMA):

```json
{
  "periods": [10, 20, 50],
  "price_source": "CLOSE",
  "normalize": {"atr_period": 14}
}
```

**Примечания по использованию**

* Только `INSERT`: обновления/удаления на уровне DAO запрещены (append-only).
* При запуске расчётов используется процедура `resolveOrCreate`: канонизация JSON → поиск по `UNIQUE` → при отсутствии — вставка.

---

### 2.7 `signal_params` — реестр параметров сигналов (append-only)

**Назначение:** аналогично `indicator_params`, но для генерации/интерпретации **сигналов** (порогов, правил входа/выхода, дебаунса и т.д.).

| Колонка             | Тип         | NULL | Default | Комментарий                                                                  |
| ------------------- | ----------- | ---- | ------- | ---------------------------------------------------------------------------- |
| id                  | BIGSERIAL   | NO   | —       | PK.                                                                          |
| type                | TEXT        | NO   | —       | Тип сигналов/правил (например: `EMA_TREND`, `MACD_IMPULSE`, `RSI_BREAKOUT`). |
| timeframe_canonical | TEXT        | YES  | —       | Канонический ТФ (если привязка требуется).                                   |
| version             | TEXT        | NO   | —       | Версия конфигурации.                                                         |
| canonical_json      | JSONB       | NO   | —       | Канонизированный JSON параметров сигналов.                                   |
| created_at          | TIMESTAMPTZ | NO   | NOW()   | Время вставки (UTC).                                                         |
| created_by          | TEXT        | YES  | —       | Автор/сервис.                                                                |
| is_active           | BOOLEAN     | NO   | TRUE    | Флаг активности.                                                             |

**Ограничения и индексы**

* `UNIQUE (type, timeframe_canonical, version, canonical_json)`.
* Индекс `BTREE (type, timeframe_canonical, version)` — быстрый выбор набора.

**Пример `canonical_json`** (RSI-сигналы):

```json
{
  "rsi_period": 14,
  "entry": {"oversold": 30, "overbought": 70},
  "debounce_bars": 2,
  "exit_on_opposite": true
}
```

**Примечания**

* Тот же `resolveOrCreate`-паттерн; изменения — только через новую версию.

---

### 2.8 `quorum_params` — параметры кворума (append-only)

**Назначение:** версия и канонический снимок весов/порогов кворума для агрегации сигналов.

| Колонка        | Тип         | NULL | Default | Комментарий                                |
| -------------- | ----------- | ---- | ------- | ------------------------------------------ |
| id             | BIGSERIAL   | NO   | —       | PK.                                        |
| version        | TEXT        | NO   | —       | Версия кворума.                            |
| canonical_json | JSONB       | NO   | —       | Канонический конфиг весов/порогов/режимов. |
| created_at     | TIMESTAMPTZ | NO   | NOW()   | Время вставки (UTC).                       |
| created_by     | TEXT        | YES  | —       | Автор/сервис.                              |
| is_active      | BOOLEAN     | NO   | TRUE    | Флаг актуальности.                         |

**Ограничения и индексы**

* `UNIQUE (version, canonical_json)`.
* Индекс `BTREE (version)` для быстрого доступа.

**Пример `canonical_json`**

```json
{
  "weights": {"EMA_TREND": 0.4, "MACD_IMPULSE": 0.3, "RSI_BREAKOUT": 0.3},
  "thresholds": {"enter": 0.6, "exit": 0.2},
  "gatekeepers": ["HTF_TREND_OK", "NO_SQUEEZE"],
  "mode_switch": {"on_squeeze": {"MACD_IMPULSE": 0.5}}
}
```

**Примечания**

* На этапе бэктеста/автотюнинга создаются **новые** версии с записью в эту таблицу (без UPDATE).

---

### 2.9 `risk_params` — параметры риск-менеджмента (append-only)

**Назначение:** хранит версионные конфиги рисков, применимые к стратегиям/боту.

| Колонка        | Тип         | NULL | Default | Комментарий                                                                  |
| -------------- | ----------- | ---- | ------- | ---------------------------------------------------------------------------- |
| id             | BIGSERIAL   | NO   | —       | PK.                                                                          |
| version        | TEXT        | NO   | —       | Версия risk-конфига.                                                         |
| canonical_json | JSONB       | NO   | —       | Канонический JSON (например: риск на сделку, лимиты просадки, стоп-правила). |
| created_at     | TIMESTAMPTZ | NO   | NOW()   | Время вставки (UTC).                                                         |
| created_by     | TEXT        | YES  | —       | Автор/сервис.                                                                |
| is_active      | BOOLEAN     | NO   | TRUE    | Флаг актуальности.                                                           |

**Ограничения и индексы**

* `UNIQUE (version, canonical_json)`.
* Индекс `BTREE (version)`.

**Пример `canonical_json`**

```json
{
  "risk_per_trade_pct": 1.0,
  "max_consecutive_losses": 3,
  "leverage_max": 10,
  "margin_mode": "ISOLATED",
  "trailing_stop": {"atr_k": 1.5, "breakeven_after_R": 1.0}
}
```

**Примечания**

* При достижении `max_consecutive_losses` торги останавливаются до ручного разрешения (логика в приложении; хранится как часть risk-конфига).

---

### 2.10 `exchange_params` — параметры интеграции биржи (append-only)

**Назначение:** хранит версии конфигов для конкретной биржи (лимиты, комиссии, эндпойнты, особенности округления и пр.).

| Колонка        | Тип         | NULL | Default | Комментарий                             |
| -------------- | ----------- | ---- | ------- | --------------------------------------- |
| id             | BIGSERIAL   | NO   | —       | PK.                                     |
| exchange_id    | BIGINT      | NO   | —       | FK → `exchange.id`.                     |
| version        | TEXT        | NO   | —       | Версия exchange-конфига.                |
| canonical_json | JSONB       | NO   | —       | Канонизированный JSON параметров биржи. |
| created_at     | TIMESTAMPTZ | NO   | NOW()   | Время вставки (UTC).                    |
| created_by     | TEXT        | YES  | —       | Автор/сервис.                           |
| is_active      | BOOLEAN     | NO   | TRUE    | Флаг актуальности.                      |

**Ограничения и индексы**

* `UNIQUE (exchange_id, version, canonical_json)` — одна версия/снимок на биржу.
* Индексы: `BTREE (exchange_id)`, `BTREE (version)`, опционально `BTREE (exchange_id, is_active)` для быстрого выбора актуальной.

**Пример `canonical_json`** (фрагмент для OKX)

```json
{
  "api": {"baseUrl": "https://www.okx.com", "rateLimits": {"reqPerSec": 10}},
  "fees": {"taker": 0.0008, "maker": 0.0002},
  "rounding": {"price_step": "0.1", "qty_step": "0.001"},
  "timeframes": {"1 hour": "1H", "1 day": "1D"}
}
```

**Примечания**

* Привязка к конкретной бирже через FK; каскадов на удаление нет (по умолчанию `RESTRICT`).
* Используется коннекторами/сервисами при вызове внешних API и преобразовании канонических значений.

---

### 2.11 `indicator_checkpoint` — **по серии**

| Колонка             | Тип         | NULL | Описание                      |
| ------------------- | ----------- | ---- | ----------------------------- |
| id                  | BIGSERIAL   | NO   | PK                            |
| indicator           | TEXT        | NO   | Имя/код индикатора            |
| history_group_id    | BIGINT      | NO   | FK→`history_group.id`         |
| indicator_params_id | BIGINT      | NO   | FK→`indicator_params.id`      |
| version             | TEXT        | NO   | Версия алгоритма              |
| last_ts             | TIMESTAMPTZ | NO   | Последний обработанный ts UTC |
| updated_at          | TIMESTAMPTZ | NO   | NOW()                         |

**UNIQUE:** `(indicator, history_group_id, indicator_params_id, version)`.

### 2.11 `exchange_instrument_coverage` — окно полного покрытия по связке

| Колонка                | Тип         | NULL | Описание                                                                                                     |
| ---------------------- | ----------- | ---- | ------------------------------------------------------------------------------------------------------------ |
| exchange_instrument_id | BIGINT      | NO   | **PK, FK**→`exchange_instrument.id` **ON DELETE CASCADE**                                                    |
| coverage_start_utc     | TIMESTAMPTZ | NO   | Начало окна, с которого **все требуемые ТФ** по связке покрыты                                               |
| coverage_end_utc       | TIMESTAMPTZ | YES  | Конец окна полного покрытия (если ограничиваем исторический интервал); `NULL` — покрытие открыто до «сейчас» |
| updated_at             | TIMESTAMPTZ | NO   | NOW()                                                                                                        |

**PK:** `(exchange_instrument_id)`.

---

### Общие замечания для всех `*_params`

* **Append-only**: только вставки; изменение параметров = новая версия.
* Канонизация JSON обязательна (стабильный порядок ключей, типы, округления/строки для вещественных).
* Рекомендуется вести аудит на уровне приложения (кто и откуда создал запись).
* Для быстрой выборки «актуальных» настроек допускается хранить `is_active = TRUE` у последней версии, но бизнес-логика не должна полагаться на это как на единственную правду (всегда можно указать `version` явно).

## 3) Индексы (V2)

* `candles (history_group_id, timestamp_utc)` — BTREE.
* `candles USING BRIN (timestamp_utc)` — длинные сканы.
* `history_group (exchange_instrument_id, timeframe_canonical)` — BTREE.
* `exchange_instrument (exchange_id, client_symbol)` — BTREE.
* Ускорители под все UNIQUE и `indicator_checkpoint` (см. ключи).

---

## 4) Ссылочная целостность и каскады

* Удаление `exchange_instrument` каскадно снесёт его `history_group` и их `candles` (через каскады).
* `exchange_instrument_coverage` — **ON DELETE CASCADE** от `exchange_instrument`.
* Прочие FK — без каскадов (RESTRICT).

---

## 5) Валидация канонических ТФ (на уровне приложения)

* При записи в `history_group`, `indicator_params`, `signal_params`, `indicator_checkpoint` — проверять, что `timeframe_canonical` ∈ `timeframes.canonical` из YAML.
* Коннектор перед запросом в биржу переводит канонику → биржевой код согласно `timeframes.exchanges.<exchange>`.

---

## 6) Алгоритм добавления новой серии (сквозной)

1. Создать `instrument`.
2. Создать `exchange_instrument`(exchange, instrument, client_symbol, contract_type, margin_mode, leverage_max).
3. На каждый канонический ТФ — `history_group(exchange_instrument_id, timeframe_canonical)`.
4. Загрузить свечи в `candles` для каждой серии, закрыть «дыры» → заполнить `history_group.coverage_start_utc`.
5. Когда все требуемые ТФ покрыты в интервале `[coverage_start_utc, coverage_end_utc || now]` — заполнить `exchange_instrument_coverage`.

---

## 7) Производительность

* BRIN по времени + BTREE `(history_group_id, timestamp_utc)` для свечей.
* Батч-вставки + `ON CONFLICT DO NOTHING` в DAO.
* Параллелизм — разделять исполнителей по ключу `(exchangeId, instrumentId)`.

---

## 8) Definition of Done (M2)

* [ ] Миграции **V1 (core_schema)** и **V2 (indexes)** по этой спецификации.
* [ ] YAML с каноническими ТФ и маппингами по биржам.
* [ ] Политика append-only на `*_params`.
* [ ] Документация в главном README/TECH_STYLE_GUIDE обновлена.
