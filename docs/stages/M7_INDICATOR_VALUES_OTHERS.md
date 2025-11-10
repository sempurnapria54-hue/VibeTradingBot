# M7 — Масштабирование значений индикаторов (MACD/RSI/Stoch/BB/OBV)

**Цель этапа.** Расширить конвейер M6 (где реализован «скелет» на EMA) до полного набора индикаторов: **MACD, RSI, Stochastic, Bollinger Bands, OBV**. Соблюсти те же принципы: канонические таймфреймы, `resolveOrCreate` параметров, checkpoint’ы, идемпотентные вставки, отсутствие триггеров, батч‑режим и строгие инварианты.

> Внимание к совместимости: M6 создал только `ema_values` (и общие реестры параметров/чекпоинты). В M7 добавляем таблицы и сервисы для MACD/RSI/Stoch/BB/OBV. Если ранее эти таблицы уже были созданы — M7 фиксирует их спецификацию и логику расчёта, без дубликатов.

---

## Зависимости и контекст

* **M2** — ядро БД: `candles`, `history_group`, `indicator_params`, `indicator_checkpoint` (generic), индексы по времени/серии.
* **M3** — коннекторы и канонические таймфреймы.
* **M4** — загрузка **закрытых** свечей.
* **M5** — целостность/покрытия: считаем индикаторы только на непрерывной части истории.
* **M6** — базовый конвейер индикаторов на EMA (правила идемпотентности/батчей/чекпоинтов/конфигов).

---

## Scope / Out of Scope

**Входит:**

* YAML‑конфигурации профилей для **MACD/RSI/Stoch/BB/OBV** (по каноническим ТФ, без дефолтов в коде).
* `resolveOrCreate` в `indicator_params` (append‑only) для каждого профиля.
* Таблицы хранения значений (см. ниже) + индексы/уникальные ключи.
* Сервисы расчёта по сериям `history_group`, warmup‑логика, checkpoint’ы.
* REST для запуска/статусов; логи/метрики.

**Не входит:**

* Сигналы (M8), бэктест/ордерный симулятор (M9), кворум/решения (M10), LIVE (M11).

---

## Конфигурация (application.yml — пример)

```yaml
indicators:
  rsi:
    - timeframe: "1 hour"
      version: v1
      params:
        period: 14
        price-source: CLOSE
        scale: 6
  macd:
    - timeframe: "1 hour"
      version: v1
      params:
        fast: 12
        slow: 26
        signal: 9
        price-source: CLOSE
        scale: 30
  stoch:
    - timeframe: "1 hour"
      version: v1
      params:
        k: 14
        d: 3
        smooth: 3
        price-source: CLOSE
        scale: 2        # 0..100
  bb:
    - timeframe: "1 hour"
      version: v1
      params:
        period: 20
        stddev: 2.0
        ma-type: SMA
        price-source: CLOSE
        scale: 30
  obv:
    - timeframe: "1 hour"
      version: v1
      params:
        price-source: CLOSE
        scale: 0
```

**Правила конфигурации:**

* Только канонические ТФ (M3). При старте валидируем, что значения соответствуют `app.timeframes.canonical`.
* Для каждый записи выполняется `resolveOrCreate` → `indicator_params` (`UNIQUE(type, timeframe_canonical, version, canonical_json)`).
* Числовые масштабы (scale) задаются явно в YAML и используются при сохранении чисел.

---

## Таблицы хранения значений (новые в M7)

> Все таблицы — **append‑only**, без UPDATE/DELETE. Идемпотентность: `INSERT … ON CONFLICT DO NOTHING`. Везде `created_at TIMESTAMPTZ NOT NULL`.

### 1) `rsi_values`

| Колонка             | Тип           | NULL | Комментарий                            |
| ------------------- | ------------- | ---- | -------------------------------------- |
| id                  | BIGSERIAL     | NO   | PK                                     |
| candle_id           | BIGINT        | NO   | FK → `candles.id`                      |
| indicator_params_id | BIGINT        | NO   | FK → `indicator_params.id` (тип `RSI`) |
| version             | TEXT          | NO   | Версия профиля                         |
| rsi_value           | NUMERIC(12,6) | NO   | Диапазон 0..100                        |
| created_at          | TIMESTAMPTZ   | NO   | —                                      |

**UNIQUE** `(candle_id, indicator_params_id, version)`
**Индексы**: `(indicator_params_id)`, `(candle_id)`

### 2) `macd_values`

| Колонка             | Тип            | NULL | Комментарий             |
| ------------------- | -------------- | ---- | ----------------------- |
| id                  | BIGSERIAL      | NO   | PK                      |
| candle_id           | BIGINT         | NO   | FK                      |
| indicator_params_id | BIGINT         | NO   | FK (тип `MACD`)         |
| version             | TEXT           | NO   | —                       |
| macd_line           | NUMERIC(50,30) | NO   | EMA(fast) − EMA(slow)   |
| signal_line         | NUMERIC(50,30) | NO   | EMA(macd_line, signal)  |
| histogram           | NUMERIC(50,30) | NO   | macd_line − signal_line |
| created_at          | TIMESTAMPTZ    | NO   | —                       |

**UNIQUE** `(candle_id, indicator_params_id, version)`

### 3) `stoch_values`

| Колонка             | Тип          | NULL | Комментарий      |
| ------------------- | ------------ | ---- | ---------------- |
| id                  | BIGSERIAL    | NO   | PK               |
| candle_id           | BIGINT       | NO   | FK               |
| indicator_params_id | BIGINT       | NO   | FK (тип `STOCH`) |
| version             | TEXT         | NO   | —                |
| k_value             | NUMERIC(6,2) | NO   | %K (0..100)      |
| d_value             | NUMERIC(6,2) | NO   | %D (0..100)      |
| created_at          | TIMESTAMPTZ  | NO   | —                |

**UNIQUE** `(candle_id, indicator_params_id, version)`

### 4) `bb_values`

| Колонка             | Тип            | NULL | Комментарий                     |
| ------------------- | -------------- | ---- | ------------------------------- |
| id                  | BIGSERIAL      | NO   | PK                              |
| candle_id           | BIGINT         | NO   | FK                              |
| indicator_params_id | BIGINT         | NO   | FK (тип `BB`)                   |
| version             | TEXT           | NO   | —                               |
| basis               | NUMERIC(50,30) | NO   | SMA(period) или база по ma‑type |
| upper               | NUMERIC(50,30) | NO   | Верхняя полоса                  |
| lower               | NUMERIC(50,30) | NO   | Нижняя полоса                   |
| bandwidth           | NUMERIC(50,30) | YES  | (upper − lower)/basis           |
| percent_b           | NUMERIC(50,30) | YES  | (close − lower)/(upper − lower) |
| created_at          | TIMESTAMPTZ    | NO   | —                               |

**UNIQUE** `(candle_id, indicator_params_id, version)`

### 5) `obv_values`

| Колонка             | Тип           | NULL | Комментарий     |
| ------------------- | ------------- | ---- | --------------- |
| id                  | BIGSERIAL     | NO   | PK              |
| candle_id           | BIGINT        | NO   | FK              |
| indicator_params_id | BIGINT        | NO   | FK (тип `OBV`)  |
| version             | TEXT          | NO   | —               |
| obv                 | NUMERIC(50,0) | NO   | Накопленный OBV |
| created_at          | TIMESTAMPTZ   | NO   | —               |

**UNIQUE** `(candle_id, indicator_params_id, version)`

> Общие замечания: FKs — `RESTRICT`; каскады удаления не требуются. Для крупных отчётов — покрывающие индексы по частым запросам.

---

## Алгоритмы расчёта (на серию `history_group`)

**Общее для всех индикаторов**

1. **Resolve params** → `indicator_params_id` (append‑only, канонизированный JSON).
2. **Старт**: если есть `indicator_checkpoint` → `startTs = last_ts + Δt`; иначе — от `coverage_start_utc` с **warmup** до длины окна.
3. **Итерация (ASC)** по свечам: поддерживать состояние окон/EMA; в зоне warmup **не писать** значения.
4. **Вставки**: батчевые `INSERT … ON CONFLICT DO NOTHING`.
5. **Чекпоинт**: после фиксации батча обновить `indicator_checkpoint.last_ts`.
6. **Параллелизм**: один писатель на ключ `(indicator, history_group_id, indicator_params_id, version)`.

**Формулы и особенности**

* **RSI(n)** — Wilder’s smoothing: `U=max(ΔC,0)`, `D=max(−ΔC,0)`, `RSI=100−100/(1+Avg(U)/Avg(D))`. Диапазон `0..100`.
* **MACD(f,s,sg)**: `MACD=EMA_f(C)−EMA_s(C)`, `Signal=EMA_sg(MACD)`, `Histogram=MACD−Signal`. Инвариант: `Histogram ≈ MACD − Signal`.
* **Stochastic(k,d,s)**: `%K_raw=100·(C−LL(k))/(HH(k)−LL(k))`, `%K=SMA(%K_raw,s)`, `%D=SMA(%K,d)`. При `HH==LL` — пропуск до появления диапазона.
* **Bollinger(period,σ)**: `Basis=SMA(period)`, `StdDev=std(period)`, `Upper=Basis+σ·StdDev`, `Lower=Basis−σ·StdDev`, `Bandwidth=(U−L)/Basis`, `%B=(C−L)/(U−L)`. Инварианты: `Upper ≥ Basis ≥ Lower`; `%B` обычно в `[0,1]`.
* **OBV**: `OBV_t=OBV_{t−1}+sign(C_t−C_{t−1})·Vol_t`; при равенстве `ΔC=0` — добавка 0.

**Numeric policy**

* Цены/линии: `NUMERIC(50,30)` по умолчанию; RSI/Stoch — `(12,6)`/`(6,2)`. Вычисления на `BigDecimal` c явным `MathContext`/`RoundingMode`.

---

## REST API (черновой контракт)

* `POST /api/indicators/fill/rsi|macd|stoch|bb|obv`
  Тело: `{ exchange:"okx", instrument:"ETH-USDT", timeframe:"1 hour", version:"v1" }`
* `POST /api/indicators/fill/others` — пакетная обработка всех профилей M7.
* `GET  /api/indicators/status?exchange=okx&instrument=ETH-USDT&timeframe=1 hour` — чекпоинты/последние рассчитанные бары.

---

## Логи и метрики

**Логи (structured):** серия/ТФ, диапазон `ts[min..max]`, warmup‑пропуски, размер батчей, `paramsId/version`, latency вставок, ошибки.
**Метрики Prometheus:**

* `indicator_values_inserted_total{type,tf}`
* `indicator_fill_batches_total{type,tf}`
* `indicator_fill_latency_ms{type,tf}`
* `indicator_checkpoint_lag_bars{type,tf}`

---

## Качество и верификация

* Временная сетка — строго каноническая (M3); только закрытые бары (M4).
* Инварианты: RSI/Stoch `0..100`, BB `Upper ≥ Basis ≥ Lower`, MACD `histogram≈diff`.
* Идемпотентность — за счёт `UNIQUE` и `ON CONFLICT DO NOTHING`.
* Повторные запуски безопасны; один писатель на ключ.

---

## Definition of Done (M7)

* [ ] YAML‑профили индикаторов MACD/RSI/Stoch/BB/OBV валидируются на старте.
* [ ] Выполнен `resolveOrCreate` по каждому профилю в `indicator_params` (append‑only).
* [ ] Созданы таблицы `macd_values`, `rsi_values`, `stoch_values`, `bb_values`, `obv_values` с ключами/индексами как выше.
* [ ] Реализованы сервисы расчёта по сериям с warmup и `indicator_checkpoint`.
* [ ] Батч‑вставки, параллелизм по сериям, один писатель на ключ.
* [ ] REST‑эндпойнты и метрики/логи готовы.
* [ ] Документация: формулы, numeric‑policy, инварианты, примеры YAML.

---

## Приложение A — Порядок миграций для M7 (описательно)

1. Создать таблицы `rsi_values`, `macd_values`, `stoch_values`, `bb_values`, `obv_values` (со спецификацией выше).
2. Добавить необходимые индексы (BTREE по FK и уникальным ключам; при больших объёмах — BRIN по времени `candles` уже есть в M2).
3. Откат = `DROP TABLE` в обратном порядке (только Dev/QA; Prod — без откатов, append‑only политика).
