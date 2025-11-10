# M8 — Сигналы

**Цель этапа.** На основе **значений индикаторов** (M6–M7) порождать **сигналы** (события) с нормированным `score ∈ [−1..+1]`, поддержкой HTF‑фильтров, дебаунса/кулдауна и подробной **объяснимостью** (`reason`). Никакой оценки сигналов, бэктеста или кворума в M8 **нет** — это будет в M9/M10.

> Терминология: «значения индикаторов» (не «фичи»). Все таймфреймы — **канонические строки** из конфигурации (см. M3). Все параметры сигналов — **append‑only** через `signal_params` с канонизированным JSON (см. M2: registry‑pattern + resolveOrCreate).

---

## Зависимости

* **M2**: таблицы `signal_params` (append‑only), базовые сущности `candles`, `history_group`, индексы по времени; (опционально) `signal_checkpoint`.
* **M3**: валидация канонических таймфреймов и их маппингов.
* **M4–M5**: непрерывная и валидная история свечей; расчёт покрытий.
* **M6–M7**: готовые таблицы значений индикаторов EMA, MACD, RSI, Stoch, BB, OBV.

---

## Scope / Out of Scope

**Входит:**

* YAML‑конфигурация правил сигналов; `resolveOrCreate` в `signal_params` (`UNIQUE(type,timeframe_canonical,version,canonical_json)`).
* Генерация **событий сигналов** на серии `history_group` (exchange_instrument × timeframe_canonical): направление `direction ∈ {−1,0,+1}`, нормированный `score`, `is_filter` и `reason`.
* Поддержка **HTF‑фильтров** (младший ТФ генерирует, старший ТФ — фильтрует) и политик **debounce/cooldown**.
* Идемпотентность и «один писатель на ключ».
* REST для запуска и статусов; логи/метрики.

**Не входит:**

* Любые метрики качества/оценки результатов, triple‑barrier и т.п. (M9).
* Кворум и торговые решения (M10).

---

## Конфигурация (application.yml — пример)

```yaml
signals:
  ema_trend:
    timeframe: "5 minutes"        # где генерируется сигнал
    version: v1
    params:
      ema-periods: [10,20,50]
      price-source: CLOSE
      htf-filter:
        timeframe: "1 hour"       # фильтр по старшему ТФ
        require: ["EMA(20)>EMA(50)"]
      debounce: {bars: 2}
      cooldown: {bars: 5}
      score:
        components:
          distance_to_ema50: {normalize: "ATR(14)", clamp: 3.0, weight: 0.7}
          ema_slope50:       {normalize: "zscore(lookback=200)", weight: 0.3}

  macd_impulse:
    timeframe: "5 minutes"
    version: v1
    params:
      macd: {fast: 12, slow: 26, signal: 9}
      entry: {histogram-rising: true, macd-above-zero: true}
      debounce: {bars: 1}
      cooldown: {bars: 3}
      score: {by: "|histogram|", normalize: "zscore(lookback=200)"}

  rsi_breakout:
    timeframe: "15 minutes"
    version: v1
    params:
      rsi-period: 14
      enter-from-bands: {oversold: 30, overbought: 70}
      midrange-neutral: [40, 60]
      debounce: {bars: 1}
      score: {by: "|RSI-50|/50"}

  bb_breakout:
    timeframe: "15 minutes"
    version: v1
    params:
      period: 20
      stddev: 2.0
      breakout:
        long:  {percent-b-min: 1.02}   # %B>1 — с запасом
        short: {percent-b-max: -0.02}  # %B<0 — с запасом
      score: {by: "|%B-0.5|"}

  obv_confirm (as-filter):
    timeframe: "5 minutes"
    version: v1
    params:
      role: FILTER # не генерирует вход, только фильтрует
      slope-min: 0.0
```

**Правила конфигурации:**

* На старте конфиг канонизируется и регистрируется в `signal_params` (resolveOrCreate).
* Валидация: все упомянутые источники (например, `EMA(50)`, `ATR(14)`, `%B`) должны быть ранее рассчитаны в M6–M7 на соответствующих ТФ.
* Никаких дефолтов в коде — все параметры в YAML.

---

## Модель данных (таблицы)

> Все таблицы — **append‑only**, без триггеров. Идемпотентность за счёт **уникальных ключей**.

### 1) `signal_event`

| Колонка          | Тип          | NULL | Комментарий                                                          |
| ---------------- | ------------ | ---- | -------------------------------------------------------------------- |
| id               | BIGSERIAL    | NO   | PK                                                                   |
| candle_id        | BIGINT       | NO   | FK → `candles.id` (бар генерации сигнала)                            |
| signal_params_id | BIGINT       | NO   | FK → `signal_params.id`                                              |
| version          | TEXT         | NO   | Версия правил сигналов                                               |
| direction        | SMALLINT     | NO   | −1 (short), 0 (neutral), +1 (long)                                   |
| score            | NUMERIC(8,4) | NO   | Нормированный вес ∈ [−1..+1]                                         |
| is_filter        | BOOLEAN      | NO   | `true`, если событие — фильтр/gatekeeper                             |
| reason           | JSONB        | YES  | Объяснимость: сработавшие условия, компоненты `score`, ссылки на HTF |
| created_at       | TIMESTAMPTZ  | NO   | Время вставки                                                        |

**UNIQUE** `(candle_id, signal_params_id, version)` — один сигнал данного типа/версии на бар.
**Индексы**: `(signal_params_id, direction)`, `(candle_id)`, `GIN(reason)` (опционально).

### 2) `signal_checkpoint` (рекомендуется)

| Колонка          | Тип         | NULL | Комментарий                               |
| ---------------- | ----------- | ---- | ----------------------------------------- |
| id               | BIGSERIAL   | NO   | PK                                        |
| signal_type      | TEXT        | NO   | Имя типа (напр. `EMA_TREND`)              |
| history_group_id | BIGINT      | NO   | Серия (`exchange_instrument × timeframe`) |
| signal_params_id | BIGINT      | NO   | Параметры сигнала                         |
| version          | TEXT        | NO   | Версия правил                             |
| last_ts          | TIMESTAMPTZ | NO   | Последняя обработанная свеча              |
| updated_at       | TIMESTAMPTZ | NO   | —                                         |

**UNIQUE** `(signal_type, history_group_id, signal_params_id, version)`.

> При желании `signal_type` можно косвенно получить через FK на `signal_params`, но явное поле упрощает ключ и отчётность.

---

## Алгоритм генерации сигналов (серия `history_group`)

1. **Resolve params** → `signal_params_id`.
2. **Определить старт**:

    * если есть `signal_checkpoint` → `startTs = last_ts + Δt`;
    * иначе `startTs = coverage_start_utc` серии (M5) + **warmup**, если правила требуют окно.
3. **Итерация по свечам (ASC)**:

    * Подтянуть необходимые **значения индикаторов** из M6–M7 (локальный ТФ) и **HTF‑значения** (последний закрытый бар HTF ≤ текущего LTF‑ts).
    * Вычислить **условия входа/выхода/нейтрали** и компоненты `score`.
    * Применить **debounce**: если предыдущее событие того же направления на расстоянии < `debounce.bars` — пропустить.
    * Применить **cooldown**: после любого события удерживать паузу `cooldown.bars`.
    * Применить **HTF‑фильтр**: если условия не выполнены — пометить `is_filter=true` и/или не эмитить (согласно политике профиля).
    * Сформировать `reason` (структурированный JSON):

      ```json
      {"ema": {"10>20>50": true, "distance": 1.8}, "atr": 14, "htf": {"tf": "1 hour", "ema20>ema50": true}, "debounced": false}
      ```
    * Выполнить `INSERT … ON CONFLICT DO NOTHING` в `signal_event`.
4. **Чекпоинт**: после успешной фиксации батча обновить/вставить `signal_checkpoint.last_ts`.
5. **Параллелизм**: «один писатель на ключ» `(signal_type, history_group_id, signal_params_id, version)`; разные серии — параллельно.

---

## Правила и инварианты (примеры)

* **EMA Trend**:

    * long: `Close > EMA(50)` и `EMA(10) > EMA(20) > EMA(50)`;
    * short: `Close < EMA(50)` и `EMA(10) < EMA(20) < EMA(50)`;
    * `score ~ clamp(|Close−EMA(50)|/ATR(14), 0..3)` с `tanh`‑сжатием.
* **MACD Impulse**: `Histogram` растёт ≥2 бара и `MACD>0` → long; обратное → short; `score ~ |Histogram|` (z‑score нормализация).
* **RSI Breakout**: выход из 30/70 + подтверждение 40/60; `score ~ |RSI−50|/50`.
* **BB Breakout**: `%B>1` (long) / `%B<0` (short); `score ~ |%B−0.5|`.
* **OBV Confirm** (filter): наклон OBV ≥ 0 (для long) / ≤ 0 (для short) — иначе `is_filter=true`.

Инварианты:

* Без look‑ahead: используем только значения индикаторов с `ts ≤ текущей свече`; HTF — последний закрытый ≤ LTF‑ts.
* `score ∈ [−1..+1]`; составляющие нормализуются до сопоставимых шкал (ATR/z‑score/мин‑макс) и взвешиваются.
* Каноническая сетка ТФ; все времена — **UTC**.

---

## REST API (черновой контракт)

* `POST /api/signals/generate`
  Тело: `{ exchange:"okx", instrument:"ETH-USDT", timeframe:"5 minutes", type:"EMA_TREND", version:"v1" }`
* `POST /api/signals/generate/all` — по всем профилям из YAML.
* `GET  /api/signals/status?exchange=okx&instrument=ETH-USDT&timeframe=5 minutes` — последние `signal_checkpoint` по типам.
* `GET  /api/signals/last?exchange=okx&instrument=ETH-USDT&timeframe=5 minutes&type=EMA_TREND&limit=100` — последние события.

---

## Логи и метрики

**Логи (structured):** старт/завершение серии, рамка `ts[min..max]`, размер батчей, доля отклонённых дебаунсом/кулдауном/HTF‑фильтром, ключи конфигов.

**Prometheus:**

* `signal_events_total{type,tf,direction}`
* `signal_debounced_total{type,tf}`
* `signal_cooldown_blocked_total{type,tf}`
* `signal_filtered_htf_total{type,tf}`
* `signal_checkpoint_lag_bars{type,tf}`

---

## Качество и explainability

* Поле `reason` — **обязательно** для каждого события (даже если фильтр/дебаунс, тогда фиксируем причину отказа).
* В reason хранить: перечень сработавших правил, компоненты и веса `score`, ссылки на опорные значения индикаторов (с их `ts`), флаги HTF.
* Должны быть интеграционные проверки инвариантов (в отдельном этапе тестирования).

---

## Definition of Done (M8)

* [ ] YAML‑профили сигналов валидируются на старте, ссылкаются только на уже рассчитанные индикаторы.
* [ ] Реализован `resolveOrCreate` для `signal_params` и используется в генераторе.
* [ ] Создана/задокументирована таблица `signal_event`; (рекомендовано) `signal_checkpoint`.
* [ ] Поддержаны HTF‑фильтры, debounce, cooldown; нормализация `score` и JSON‑`reason`.
* [ ] Идемпотентные вставки (`ON CONFLICT DO NOTHING`), «один писатель на ключ», параллельная обработка разных серий.
* [ ] REST‑эндпойнты и метрики готовы.
* [ ] Документация с примерами YAML и инвариантами без look‑ahead.

---

## Что дальше

* **M9 — Бэктест и метрики:** поверх `signal_event` запустим оффлайн‑симулятор исполнения (trailing SL/TP, частичные фиксации, комиссии, slippage, funding) и получим метрики эффективности.
* **M10 — Кворум и торговые решения:** после M9, на основе качественных сигналов и их метрик, сконфигурируем веса/порог и начнём принимать торговые решения.
