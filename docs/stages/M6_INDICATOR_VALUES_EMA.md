# M6 — Значения индикаторов (EMA как «скелет» конвейера)

**Цель этапа.** Запустить минимально‑жизнеспособный конвейер расчёта **значений индикаторов** на примере **EMA**: конфиг → `resolveOrCreate` параметров → расчёт по сериям `history_group` → сохранение в `ema_values` → чекпоинты и идемпотентность. Это скелет, который далее масштабируется в M7 (MACD/RSI/Stoch/BB/OBV).

> Терминология: «значения индикаторов» (не «фичи»). Работает только на **закрытых** свечах. Таймфреймы — **канонические строки** из конфигурации (см. M3). Никаких триггеров в БД; всё идемпотентно через уникальные ключи.

---

## Зависимости

* **M2**: таблицы `candles`, `history_group`, `indicator_params` (append‑only), `indicator_checkpoint`, индексы.
* **M3**: канонические таймфреймы и проверка маппингов по биржам.
* **M4–M5**: валидная и непрерывная история свечей + покрытия.

EMA считаем **только внутри покрытия** соответствующей серии.

---

## Scope / Out of Scope

**Входит:**

* YAML‑конфигурации EMA‑профилей (по каноническим ТФ; без дефолтов в коде).
* `resolveOrCreate` в `indicator_params` (append‑only) с канонизированным JSON.
* Расчёт EMA по сериям `history_group` с учётом **warmup** и **checkpoint**.
* Таблица хранения `ema_values` (append‑only, `UNIQUE`).
* REST для запуска/статусов; логи и метрики.

**Не входит:**

* Любые другие индикаторы (в M7).
* Сигналы/бэктест/кворум/ордера.

---

## Конфигурация (application.yml — пример)

> В коде нет дефолтов. Все значения читаются из YAML. Таймфреймы — из `app.timeframes.canonical`.

```yaml
indicators:
  ema:
    - timeframe: "5 minutes"
      version: v1
      params:
        periods: [10, 20, 50]
        price-source: CLOSE         # CLOSE | HL2 | HLC3 | OHLC4
        scale: 30                   # BigDecimal scale при сохранении
    - timeframe: "1 hour"
      version: v1
      params:
        periods: [10, 20, 50]
        price-source: CLOSE
        scale: 30
```

**Правила конфигурации**

* На старте: канонизация JSON → `resolveOrCreate` в `indicator_params` (`UNIQUE(type, timeframe_canonical, version, canonical_json)`).
* Для EMA допускается несколько периодов в одном профиле (`periods: [10,20,50]`).
* `price-source` фиксируется и участвует в канонизационном JSON (часть реплицируемости).

---

## Схема хранения `ema_values`

> Таблица одна — только для EMA. Остальные индикаторы добавятся в M7.

| Колонка             | Тип            | NULL | Комментарий                            |
| ------------------- | -------------- | ---- | -------------------------------------- |
| id                  | BIGSERIAL      | NO   | PK                                     |
| candle_id           | BIGINT         | NO   | FK → `candles.id`                      |
| indicator_params_id | BIGINT         | NO   | FK → `indicator_params.id` (тип `EMA`) |
| version             | TEXT           | NO   | Версия алгоритма/правил сохранения     |
| period              | INTEGER        | NO   | Период EMA из массива `periods`        |
| ema_value           | NUMERIC(50,30) | NO   | Значение EMA                           |
| created_at          | TIMESTAMPTZ    | NO   | Время вставки                          |

**UNIQUE** `(candle_id, indicator_params_id, version, period)`
**Индексы**: `(indicator_params_id, period)`, `(candle_id)`

> Политика чисел: цены/EMA — `NUMERIC(50,30)`; вычисления на `BigDecimal` с явным `MathContext`/RoundingMode.

---

## Алгоритм расчёта (на серию `history_group`)

1. **Resolve params**: из YAML получить/создать запись в `indicator_params` → `indicator_params_id`.
2. **Стартовая точка**:

    * если есть `indicator_checkpoint` для `(EMA, history_group_id, indicator_params_id, version)` → `startTs = last_ts + Δt`;
    * иначе `startTs = max(history_group.coverage_start_utc, firstAvailableTs)` и включить **warmup** (набор окна под максимальный `period`).
3. **Итерация по свечам (ASC)**:

    * поддерживать буфер SMA/EMA для каждого периода из `periods`;
    * в зоне warmup — **не писать** значения (только обновлять состояние);
    * за пределом warmup — формировать батч записей (`period`, `ema_value`) и выполнять `INSERT … ON CONFLICT DO NOTHING`.
4. **Чекпоинт**: после фиксации батча обновить/вставить `indicator_checkpoint.last_ts`.
5. **Идемпотентность**: повторные запуски безопасны — уникальный ключ защищает от дублей.
6. **Параллелизм**: один писатель на ключ `(indicator=EMA, history_group_id, indicator_params_id, version)`; разные серии — параллельно.

---

## Формулы и инварианты EMA

* `α = 2/(n+1)`; `EMA_0 = SMA(n)` (seed от первых `n` баров `price-source`).
* Рекурсия: `EMA_t = α·P_t + (1−α)·EMA_{t−1}`.
* Для нескольких периодов в профиле буферы независимы, но читают тот же `price-source`.
* Временная сетка — строго каноническая (см. M3); только закрытые бары (см. M4).
* Инвариант точности: пересчёты при одинаковом конфиге и версии дают идентичные значения.

---

## REST API (черновой контракт)

* `POST /api/indicators/fill/ema`
  Тело: `{ exchange:"okx", instrument:"ETH-USDT", timeframe:"1 hour", version:"v1" }` — расчёт EMA по серии.
* `POST /api/indicators/fill/ema/bulk`
  Тело: список таких же заданий — пакетный запуск по нескольким сериям.
* `GET  /api/indicators/status/ema?exchange=okx&instrument=ETH-USDT&timeframe=1 hour` — последний `checkpoint` и рамка рассчитанных баров.

---

## Логи и метрики

**Логи (structured)**: начало/конец серии, `ts[min..max]`, warmup‑пропуски, размер батчей, `paramsId/version`, latency вставок, ошибки.
**Метрики Prometheus:**

* `ema_values_inserted_total{tf}`
* `ema_fill_batches_total{tf}`
* `ema_fill_latency_ms{tf}`
* `indicator_checkpoint_lag_bars{indicator="EMA",tf}`

---

## Качество и верификация

* Проверка сетки и OHLC выполнена в M5; в M6 проверяем только непротиворечивость входных данных и корректность `price-source`.
* На seed‑отрезке `SMA(n)` для каждого периода сверяется с контрольным пересчётом (интеграционные проверки вне этого документа).

---

## Definition of Done (M6)

* [ ] YAML‑профили EMA валидируются на старте (таймфрейм ∈ каноника, типы/масштабы корректны).
* [ ] `resolveOrCreate` для `indicator_params` реализован и используется.
* [ ] Таблица `ema_values` создана со схемой выше; ключи/индексы присутствуют.
* [ ] Сервис расчёта по сериям с warmup и `indicator_checkpoint` реализован.
* [ ] Идемпотентные батч‑вставки без триггеров; один писатель на ключ.
* [ ] REST‑эндпойнты и метрики готовы.
* [ ] Документация: формулы, numeric‑policy, пример YAML.

---

## Приложение A — Параметры точности

* По умолчанию `scale=30` для EMA и цен; округление `HALF_UP`.
* `price-source` влияет на воспроизводимость и должен фиксироваться в `canonical_json` профиля.
