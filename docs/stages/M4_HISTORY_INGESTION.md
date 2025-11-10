# M4 — Загрузка истории свечей (бэкфилл, инкремент, докачка, покрытия)

**Цель этапа.** На базе интеграции M3 загрузить и поддерживать исторические **закрытые свечи** по сериям `exchange_instrument × timeframe_canonical`, гарантировать идемпотентность и целостность, а также вычислять локальные и сводные **покрытия** (см. M2: `history_group.coverage_start_utc`, `exchange_instrument_coverage.coverage_start_utc/coverage_end_utc`).

> В этом этапе **нет расчёта индикаторов/сигналов** — это M4 только про свечи. Коннекторы, подписи, rate‑limit и т.д. — см. M3. Схема БД — см. M2. Мы работаем только с **закрытыми** свечами.

---

## Scope / Out of Scope

**Входит:**

* Инициализация серий (`history_group`) по каноническим ТФ.
* **Бэкфилл** (полная загрузка «назад»), **инкрементальная** догрузка («вперёд»), **докачка дыр** (gap fill).
* Обновление `history_group.coverage_start_utc` и `exchange_instrument_coverage`.
* Идемпотентность: `UNIQUE (history_group_id, timestamp_utc)` + `INSERT … ON CONFLICT DO NOTHING`.
* Контроллеры REST для ручного запуска/статусов; логи и метрики.

**Не входит:**

* Индикаторы (M4+ индикаторы — в отдельном этапе), сигналы/кворум/ордера — последующие этапы.

---

## Зависимости (из M2/M3)

* Таблицы M2: `exchange`, `instrument`, `exchange_instrument`, `history_group`, `candles`, `exchange_instrument_coverage`.
* Канонические таймфреймы и их маппинг по биржам — M3 (YAML).
* SPI M3: `ExchangeConnector`, `TimeframeTranslator`, `ExchangeClock`.

---

## Конфигурация (application.yml — пример)

```yaml
app:
  timeframes:
    canonical: ["1 minute","3 minutes","5 minutes","15 minutes","1 hour","2 hours","4 hours","1 day"]
  ingestion:
    batch:
      size: 100                 # запрашиваемый размер батча у API
    pacing:
      pause-ms: 300             # технологическая пауза между батчами (если нужна)
    retry:
      max-attempts: 3
      backoff-ms: 1000
    rate-limit:
      enabled: true
      permits-per-second: 8
      timeout-ms: 2000
    parallelism:
      series-writers: 4         # максимум одновременных писателей по разным сериям
      key: "{exchangeId}:{exchangeInstrumentId}:{timeframeCanon}"

# см. M3 — у каждой биржи свой раздел и свой map таймфреймов
okx:
  base-url: https://www.okx.com
  api-key: ${OKX_KEY}
  secret-key: ${OKX_SECRET}
  passphrase: ${OKX_PASSPHRASE}
  timeframes:
    "1 minute":  "1m"
    "3 minutes": "3m"
    "5 minutes": "5m"
    "15 minutes": "15m"
    "1 hour":    "1H"
    "2 hours":   "2H"
    "4 hours":   "4H"
    "1 day":     "1D"
```

**Правила:**

* **Нет дефолтов в коде** — всё из YAML.
* Любая серия вычисляется только на **каноническом** ТФ; код биржи берётся из `<exchange>.timeframes`.

---

## Модели данных и инварианты

* `candles`: **только закрытые** бары; `timestamp_utc` — начало интервала; типы — `NUMERIC(50,30)` для цен/объёмов.
* Уникальность: `(history_group_id, timestamp_utc)`.
* Сетка времени по ТФ: равномерная; при вставке проверяем, что `timestamp_utc` кратен шагу ТФ (валидируем на уровне приложения).
* Валидации OHLC: `low ≤ min(open, close) ≤ max(open, close) ≤ high`; объёмы ≥ 0.

---

## Алгоритмы

### A) Инициализация серии

**Задача:** подготовить `history_group`(ы) для связки `exchange_instrument` по списку канонических ТФ.

1. Проверить/создать `exchange_instrument` (по `exchange`, `instrument`, `client_symbol`).
2. Для каждого ТФ из списка: upsert `history_group(exchange_instrument_id, timeframe_canonical)`.
3. Возвратить список series‑id для последующей загрузки.

### B) Бэкфилл (полная загрузка истории «назад»)

**Идея:** идём **назад по времени** батчами до конца истории API.

1. `series := history_group`.
2. `cursor := now()` (UTC) — **исключительная** верхняя граница.
3. В цикле: вызвать `connector.loadClosedCandles(symbol, tfCode, beforeExclusive=cursor, limit=batch.size)`.
4. Преобразовать в доменные бары и выполнить batched `INSERT … ON CONFLICT DO NOTHING` в `candles`.
5. Если батч пуст/меньше `limit` — выйти. Иначе `cursor := min(ts) из батча`.
6. После завершения: рассчитать **локальное покрытие серии** (см. D) и обновить `history_group.coverage_start_utc` (если пусто).

### C) Инкрементальная догрузка (актуализация «вперёд»)

1. Для серии взять `latestTs := max(timestamp_utc)`.
2. Запросить у коннектора **новые закрытые** бары после `latestTs` (реализация может использовать `beforeExclusive=now()` и фильтрацию по времени).
3. Вставить upsert‑ом. Если нет новых — серия актуальна.
4. При наличии **окна полного покрытия** (см. D) обновить сводное покрытие по связке.

### D) Докачка «дыр» (gap fill)

**Вход:** `fromInclusive`, `toExclusive`.

1. Сгенерировать ожидаемую сетку отметок времени для этого ТФ на `[from, to)` и вычесть **имеющиеся** `timestamp_utc` из БД.
2. Если список пуст — ничего делать не нужно.
3. Выкачать недостающие интервалы батчами (не обязательно по одной свече).
4. Вставить upsert‑ом; затем пересчитать локальное и сводное покрытие.

### E) Покрытия

**Локальное покрытие серии (`history_group.coverage_start_utc`):**

* Определяется как **минимальный** `timestamp_utc` в серии **после** закрытия всех «дыр» в начале.
* Можно считать как первую точку, с которой сетка времени **непрерывна** до «сейчас» (для данного ТФ).

**Сводное покрытие по связке (`exchange_instrument_coverage`):**

* Взять набор **обязательных** ТФ (из конфигурации для анализа/торговли).
* `coverage_start_utc := max(всех history_group.coverage_start_utc по этим ТФ)`.
* `coverage_end_utc := min(последних закрытых свечей по этим ТФ)`; `NULL` — если идёт до «сейчас».
* Upsert по ключу `exchange_instrument_id`.

---

## Идемпотентность и конкуренция

* Вставки только через `INSERT … ON CONFLICT (history_group_id, timestamp_utc) DO NOTHING`.
* **Один писатель на серию**: ключ координации `(exchangeId, exchangeInstrumentId, timeframeCanonical)`; параллелизм — по разным сериям/связкам.
* Повторы запросов к API допустимы (дедупликацию делает БД).

---

## Обработка ошибок / устойчивость

* `Retry` только для транзиентных сбоев (сетевые/5xx/timeout) с экспоненциальным backoff.
* `RateLimiter` на вызовы API; **без `Thread.sleep()`** — ожидания через разрешения лимитера/планировщик.
* Классификация ошибок API биржи (rate‑limit, invalid params, not found) с понятными сообщениями в логах.

---

## REST API (черновой контракт)

* `POST /api/history/series/init`
  Тело: `{ exchange:"okx", instrument:"ETH-USDT", timeframes:["1 hour","1 day"] }`
  Действие: создать `exchange_instrument` (если нужно) и `history_group`(ы).

* `POST /api/history/fill/backfill`
  Тело: `{ exchange:"okx", instrument:"ETH-USDT", timeframe:"1 hour" }`

* `POST /api/history/fill/incremental`
  Тело: `{ exchange:"okx", instrument:"ETH-USDT", timeframe:"1 hour" }`

* `POST /api/history/fill/gaps`
  Тело: `{ exchange:"okx", instrument:"ETH-USDT", timeframe:"1 hour", from:"2024-01-01T00:00:00Z", to:"2024-03-01T00:00:00Z" }`

* `GET /api/history/coverage?exchange=okx&instrument=ETH-USDT`
  Ответ: сводное покрытие `coverage_start_utc`, `coverage_end_utc`.

* `GET /api/history/series/status?exchange=okx&instrument=ETH-USDT`
  Ответ: по каждой серии — `last_ts`, `coverage_start_utc`.

---

## Логи и метрики

**Логи (structured):** размер батча, `ts[min..max]`, ожидание rate‑limit, коды/сообщения ошибок, повторные попытки.
**Метрики Prometheus:**

* `history_candles_fetched_total{exchange,tf}`
* `history_candles_inserted_total{exchange,tf}`
* `history_ingestion_batch_latency_ms{exchange,tf}`
* `history_ingestion_api_errors_total{exchange,endpoint}`
* `history_rate_limit_wait_ms{exchange,endpoint}`

---

## Качество данных: проверки

* Время — только **UTC**; `timestamp_utc` не должен быть > `nowExchange()` + допуск.
* Шаг сетки: `Δt` соответствует каноническому ТФ.
* Инварианты OHLC/объёмов (см. «Модели данных и инварианты»).
* Отсутствие дубликатов гарантирует уникальный ключ.

---

## Псевдокод (бэкфилл)

```
series = findOrCreateHistoryGroup(exchangeInstrumentId, timeframeCanon)
cursor = nowUtc()
while true:
  candles = connector.loadClosedCandles(symbol, toExchangeCode(exchangeId, timeframeCanon), beforeExclusive=cursor, limit=batchSize)
  if candles.isEmpty(): break
  upsertCandles(series.id, candles)      # ON CONFLICT DO NOTHING
  cursor = min(candles.ts)
  rateLimiter.acquire()                  # без Thread.sleep()
recomputeLocalCoverage(series.id)
recomputeAggregateCoverage(exchangeInstrumentId)
```

---

## Definition of Done (M4)

* [ ] REST‑эндпойнты для init/backfill/incremental/gaps/status/coverage.
* [ ] Бэкфилл/инкремент/докачка реализованы и идемпотентны.
* [ ] Алгоритмы локального и сводного покрытия (start/end) реализованы.
* [ ] Логи/метрики покрывают основные сценарии; ошибки классифицированы.
* [ ] Валидации сетки ТФ, OHLC и времени включены; UTC соблюдается.
* [ ] Параллелизм по сериям; **single‑writer** на серию; ожидания без `Thread.sleep()`.

---

## Приложение A — Онбординг нового инструмента (сквозной сценарий)

1. `POST /api/history/series/init {exchange, instrument, timeframes}` — создаёт `exchange_instrument` и `history_group`(ы).
2. `POST /api/history/fill/backfill {…}` — полный бэкфилл для каждой серии.
3. `POST /api/history/fill/gaps {…}` — точечная докачка (при необходимости).
4. `POST /api/history/fill/incremental {…}` — актуализация перед расчётом индикаторов.
5. `GET /api/history/coverage?…` — контроль готовности окна для аналитики/торговли.
