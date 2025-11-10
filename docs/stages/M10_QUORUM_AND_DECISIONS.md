# M10 — Кворум и торговые решения

**Цель этапа.** Объединить отдельные **сигналы** (M8) в единое **решение** с помощью кворума: взвешенная сумма `S = Σ wᵢ·scoreᵢ` + **gatekeeper‑фильтры** и пороги. По результатам бэктеста (M9) — **настроить веса/пороги** (автотюнинг) и выпускать **решения**: `ENTER / EXIT / REVERSE / MODIFY_SLTP / HOLD` с объяснимостью. На M10 **не создаются реальные ордера** — только решение и рекомендуемые параметры (M11 исполнит).

> Терминология: «значения индикаторов» (M6–M7) → «сигналы» (M8) → **кворумные решения** (M10) → «ордера/позиции LIVE» (M11). Все таймфреймы — **канонические строки** из конфига (см. M3). Параметры кворума — **append‑only** через `quorum_params`.

---

## Зависимости

* **M2**: реестр `quorum_params` (append‑only), базовые сущности `candles`, `history_group`.
* **M3**: канонические таймфреймы, валидации конфига при старте.
* **M4–M5**: целостная история и покрытия.
* **M6–M7**: таблицы значений индикаторов (для нормализаций/режимов).
* **M8**: `signal_event` (кандидаты входа/выхода) + (рекомендовано) `signal_checkpoint`.
* **M9**: агрегаты эффективности сигналов и стратегии — источник для автотюнинга весов/порогов.

---

## Scope / Out of Scope

**Входит:**

* YAML‑конфиг кворума → `resolveOrCreate` в `quorum_params` (append‑only, канонизированный JSON).
* Подсчёт **композитного score** `S` из нескольких сигналов (в т.ч. разных ТФ), применение gatekeeper‑фильтров.
* Пороги и политики принятия решений: `ENTER`, `EXIT`, `REVERSE`, `MODIFY_SLTP`, `HOLD`.
* Объяснимость (reason): вклад каждого сигнала, веса, блокирующие фильтры, итоговое решение.
* **Автотюнинг** весов/порогов по результатам M9 (новая версия `quorum_params`).
* REST для запуска/статусов; метрики/логи.

**Не входит:**

* Создание реальных ордеров/позиционных объектов (M11).

---

## Конфигурация (application.yml — пример)

```yaml
quorum:
  version: v1
  timeframe: "5 minutes"           # где принимается решение
  inputs:
    - { type: EMA_TREND,   version: v1, weight: 0.45, minScore: 0.30 }
    - { type: MACD_IMPULSE,version: v1, weight: 0.35, minScore: 0.25 }
    - { type: RSI_BREAKOUT,version: v1, weight: 0.20, minScore: 0.20 }
  gatekeepers:
    - { type: OBV_CONFIRM, timeframe: "5 minutes", require: ["slope >= 0"], effect: BLOCK_IF_FAIL }
    - { type: HTF_EMA, timeframe: "1 hour", require: ["EMA(20) > EMA(50)"], effect: DOWNWEIGHT:0.5 }
  thresholds:
    enter_long:  +0.60
    enter_short: -0.60
    exit_to_flat: 0.00             # можно держать порог на выход
    reverse_delta: 0.80            # насколько S должен «перекрыть» противоположную сторону
  actions:
    modify_sltp:
      enable: true
      when:
        - { condition: "S increases by >= 0.2 within K bars", action: "tighten SL by 0.5*ATR" }
        - { condition: "S drops below 0.2", action: "loosen SL back to base" }
  cooldown:
    enter_bars: 3
    exit_bars:  2
  sizing_hint:                     # рекомендации для M11 (LIVE)
    risk_params_ref: default_isolated_1pct
    sl_mode: { type: ATR, k_atr: 1.0 }
    tp_levels_r: [1.0, 2.0, 3.0]
    trailing: { enabled: true, mode: ATR_TRAIL, k_atr: 2.0, breakeven_after_r: 1.0 }
```

**Правила конфигурации**

* На старте канонизируем JSON → `resolveOrCreate` в `quorum_params` (`UNIQUE(timeframe_canonical, version, canonical_json)`).
* Все указанные `inputs` и `gatekeepers` должны иметь соответствующие `signal_event` профили из M8.
* Никаких дефолтов в коде: веса/пороги/политики берём из YAML.

---

## Модель данных (таблицы)

> Все таблицы — **append‑only**, без триггеров. Идемпотентность — через уникальные ключи.

### 1) `quorum_decision_event`

| Колонка          | Тип          | NULL | Комментарий                                                      |
| ---------------- | ------------ | ---- | ---------------------------------------------------------------- |
| id               | BIGSERIAL    | NO   | PK                                                               |
| candle_id        | BIGINT       | NO   | FK → `candles.id` (бар решения на TF кворума)                    |
| quorum_params_id | BIGINT       | NO   | FK → `quorum_params.id`                                          |
| version          | TEXT         | NO   | Версия кворума                                                   |
| action           | TEXT         | NO   | `ENTER_LONG`/`ENTER_SHORT`/`EXIT`/`REVERSE`/`MODIFY_SLTP`/`HOLD` |
| composite_score  | NUMERIC(8,4) | NO   | Итоговый `S`                                                     |
| inputs           | JSONB        | NO   | Сводка: `{type,score,weight,passedMin,contrib}` по каждому входу |
| filters          | JSONB        | YES  | Gatekeeper‑результаты и эффекты (BLOCK/DOWNWEIGHT/…)             |
| recommendations  | JSONB        | YES  | Реком. SL/TP/Trailing/размер                                     |
| created_at       | TIMESTAMPTZ  | NO   | —                                                                |

**UNIQUE** `(candle_id, quorum_params_id, version)` — одно решение на бар.

### 2) `quorum_trade` (логическая сделка кворума)

| Колонка           | Тип            | NULL | Комментарий                                    |
| ----------------- | -------------- | ---- | ---------------------------------------------- |
| id                | BIGSERIAL      | NO   | PK                                             |
| open_decision_id  | BIGINT         | NO   | FK → `quorum_decision_event.id` (ENTER*)       |
| close_decision_id | BIGINT         | YES  | FK → `quorum_decision_event.id` (EXIT/REVERSE) |
| side              | TEXT           | NO   | `LONG`/`SHORT`                                 |
| entry_price       | NUMERIC(50,30) | YES  | Цена по бару решения (реф.)                    |
| exit_price        | NUMERIC(50,30) | YES  | —                                              |
| sizing            | JSONB          | YES  | Рекомендации размеров/риска                    |
| outcome           | JSONB          | YES  | Результат (при бэктесте/симе)                  |
| created_at        | TIMESTAMPTZ    | NO   | —                                              |

> В LIVE (M11) реальные ордера будут ссылаться на `quorum_decision_event`.

### 3) `quorum_aggregate` (итоги по профилю)

| Колонка             | Тип          | NULL | Комментарий                         |
| ------------------- | ------------ | ---- | ----------------------------------- |
| id                  | BIGSERIAL    | NO   | PK                                  |
| quorum_params_id    | BIGINT       | NO   | FK                                  |
| timeframe_canonical | TEXT         | NO   | Канон ТФ принятия решений           |
| n_decisions         | BIGINT       | NO   | Кол-во решений                      |
| n_enter             | BIGINT       | NO   | Кол-во входов                       |
| n_exit              | BIGINT       | NO   | Выходов                             |
| pct_filtered        | NUMERIC(8,4) | NO   | Доля заблокированных gatekeeper’ами |
| mean_S              | NUMERIC(8,4) | NO   | Средний композитный score           |
| created_at          | TIMESTAMPTZ  | NO   | —                                   |

**UNIQUE** `(quorum_params_id, timeframe_canonical)`.

---

## Алгоритм принятия решения (на серии TF кворума)

1. **Resolve params** → `quorum_params_id`.
2. **Собрать входы**: для каждого `input` выбрать **последний** `signal_event` ≤ времени текущего бара на TF кворума (и соответствующего LTF, если он ниже), проверить `minScore`.
3. **Применить gatekeeper‑фильтры**: если `BLOCK_IF_FAIL` — решение `HOLD` и зафиксировать `filters`.
4. **Посчитать S**: `S = Σ wᵢ·scoreᵢ` по прошедшим входам; опционально применить эффекты фильтров `DOWNWEIGHT:factor`.
5. **Решение**:

    * если нет открытой позиции и `S ≥ enter_long` → `ENTER_LONG`;
    * если нет позиции и `S ≤ enter_short` → `ENTER_SHORT`;
    * если есть позиция и `S` перешёл через `exit_to_flat` → `EXIT`;
    * если позиция long и `S ≤ -reverse_delta` → `REVERSE → SHORT` (закрыть long + открыть short);
    * аналогично для short.
6. **Рекомендации**: рассчитать из секции `sizing_hint` (используются `risk_params`/`ATR`/`TP/SL` правила — **без** создания ордеров).
7. **Записать** `quorum_decision_event` (`ON CONFLICT DO NOTHING`).
8. **link** к `quorum_trade` (логически открыть/закрыть сделку кворума для удобства отчётности).

**Идемпотентность и конкуренция**

* Один писатель на ключ `(history_group_id, quorum_params_id, version)`.
* Повторные прогоны безопасны (уникальные ключи). Все времена — **UTC**.

---

## Автотюнинг весов/порогов (по результатам M9)

**Идея:** выбрать веса/пороги, максимизирующие метрику (например, `expectancy_r` или `Sharpe`) при ограничениях (например, частота сделок, MaxDD).

**Процедура:**

1. Собрать из M9/`performance_aggregate` и/или агрегатов сигналов эффективность по типам, score‑бакам и режимам (тренд/флэт, ATR‑квантили).
2. Определить **кандидаты**: сетка весов, порогов `enter_long/short`, `reverse_delta`.
3. Для каждой конфигурации — запустить **быструю симуляцию решений** поверх уже рассчитанных бэктест‑результатов (без повторного симулятора исполнения) или mini‑run.
4. Выбрать **Pareto‑лучшие** по нескольким метрикам.
5. Сформировать новый `quorum_params` (новая `version`), указав `source: autotune:{runId, objective, constraints}` в `canonical_json`.

**Артефакты:** протоколы тюнинга, топ‑N конфигураций с метриками, diff веса/пороги.

---

## REST API (черновик)

* `POST /api/quorum/decide`
  Тело: `{ exchange:"okx", instrument:"ETH-USDT", timeframe:"5 minutes", version:"v1" }` — прогон кворума на диапазоне баров, сохранение `quorum_decision_event`.
* `GET  /api/quorum/last?exchange=okx&instrument=ETH-USDT&timeframe=5 minutes&limit=100` — последние решения с reason.
* `POST /api/quorum/autotune`
  Тело: `{ runId: 123, objective: "expectancy_r", constraints: { maxDD: 0.2, minTrades: 200 } }` — запуск тюнинга; результат — новая запись `quorum_params`.
* `GET  /api/quorum/aggregates?version=v1` — агрегаты `quorum_aggregate`.

---

## Логи, метрики, explainability

**Логи (structured):** рамка `ts[min..max]`, состав входов, весовые вкладки, сработавшие фильтры, итоговая `S`, принятое действие, рекомендации.

**Prometheus:**

* `quorum_decisions_total{action}`
* `quorum_blocked_total{filter}`
* `quorum_composite_score{quantile}` (summary)
* `quorum_autotune_runs_total{status}`

**Explainability:**

* `inputs`: массив `{type, tf, score, weight, contrib = weight*score, reason_ref}`.
* `filters`: массив `{type, passed, effect}`.
* `recommendations`: `{risk_ref, sl, tp[], trailing}`.
* Все ссылки восстанавливают **полную трассу**: *значения индикаторов* → *сигналы* → *кворум* → *решение* (и далее в M11 → *ордеры*).

---

## Definition of Done (M10)

* [ ] Конфигурация кворума в YAML валидируется; `resolveOrCreate` в `quorum_params`.
* [ ] Реализован вычислитель кворума: сбор входов, gatekeepers, подсчёт `S`, принятие действий, рекомендации.
* [ ] Добавлены таблицы `quorum_decision_event`, `quorum_trade`, `quorum_aggregate` (append‑only, уникальные ключи, индексы).
* [ ] REST‑эндпойнты `decide/last/autotune/aggregates` доступны.
* [ ] Метрики Prometheus и подробные логи включены.
* [ ] Автотюнинг весов/порогов по результатам M9 создаёт **новую версию** `quorum_params` с источником.
* [ ] Объяснимость на уровне записи: вклад по каждому сигналу, фильтры, итог и рекомендации.

---

## Что дальше

* **M11 — LIVE: ордера/позиции, риск.** M10 отдаёт `quorum_decision_event` с рекомендациями (размер, SL/TP/Trailing). M11 создаёт реальные ордера на бирже (изолированная маржа, ≤x10), управляет позицией и поддерживает защитные действия при серии убыточных сделок.
