# M9 — Бэктест и метрики (оффлайн‑симулятор исполнения)

**Цель этапа.** Реализовать **детерминированный оффлайн‑симулятор исполнения ордеров** поверх исторических свечей (M4–M5), который принимает **сигналы** (M8) и/или простые правила входа, применяет **рисковые и исполненческие параметры** (изолированная маржа, плечо ≤ x10, риск ≤ 1% на сделку), моделирует **комиссии, проскальзывание, спред, funding** и поддерживает **trailing SL/TP, перевод в безубыток, частичные тейк‑профиты**, Time‑in‑Force, латентность и т.п. Итог — **журналы сделок/кривые equity/метрики** и воспроизводимая конфигурация запуска.

> На M9 мы **не** принимаем торговые решения кворума (это M10). Здесь тестируется «стратегия» как набор правил входа/выхода (на базе сигналов), а также fidelity моделей исполнения.

---

## Зависимости

* **M2**: реестры `risk_params`, `exchange_params`, (опционально) `backtest_params`/`backtest_run` (см. ниже), базовые сущности.
* **M3**: канонические ТФ и валидации.
* **M4–M5**: непрерывная история свечей + покрытия по сериям.
* **M6–M7**: значения индикаторов (для ATR, %B и т.п., если используются динамические стопы/таргеты).
* **M8**: `signal_event` как кандидаты входа (direction/score/reason/HTF‑флаги).

---

## Scope / Out of Scope

**Входит:**

* YAML‑конфиг запуска бэктеста → `resolveOrCreate` в `backtest_params` (append‑only, канонизированный JSON) и создание `backtest_run`.
* Симуляция исполнения **bar‑by‑bar**: market/limit/stop‑market/stop‑limit, latency (в барах), TIF (GTC/IOC/FOK), частичные исполнения.
* R‑позиционирование (расчёт размера позиции от риска 1% и SL‑дистанции), **isolated margin** и плечо ≤ x10.
* Логика SL/TP: статические, **trailing**, перевод SL в **безубыток**, **partials** (например, TP1/TP2/TP3).
* Комиссии (maker/taker bps), **slippage** (bps/модель спреда), **funding** (если данные доступны, либо упрощённая ставка).
* Итоговые артефакты: ордера/фйлы/сделки/позиции/кривая equity/метрики по сделкам и по портфелю.

**Не входит:**

* Веса/пороги кворума и его решения (M10).
* LIVE‑торговля (M11).

---

## Конфигурация бэктеста (application.yml — пример)

```yaml
backtest:
  version: v1
  window:
    from:  "2023-01-01T00:00:00Z"
    to:    "2025-01-01T00:00:00Z"
  universe:
    - { exchange: okx, instrument: "ETH-USDT", timeframe: "5 minutes" }
  entry:
    source: SIGNALS           # SIGNALS | RULES
    signals:
      include:
        - { type: EMA_TREND, version: v1, minScore: 0.4 }
        - { type: MACD_IMPULSE, version: v1, minScore: 0.5 }
      opposite_signal_exit: true
    rules:                    # если source=RULES (простые правила без M8)
      direction: BY_EMA_RIBBON
      threshold: { distance_to_ema50_atr: 1.0 }
  risk:
    risk_params_ref: default_isolated_1pct
    max_concurrent_positions: 1
    pyramiding: false
  execution:
    latency_bars: 1           # задержка между сигналом и выставлением ордера
    order_type: MARKET        # MARKET | LIMIT | STOP_MARKET | STOP_LIMIT
    tif: GTC                  # GTC | IOC | FOK
    slippage_bps: 2           # 0.02%
    maker_fee_bps: 2         # 0.02%
    taker_fee_bps: 8         # 0.08%
    spread_model: MID        # MID | FIXED_BPS | LAST
  stops_targets:
    sl: { mode: ATR, k_atr: 1.0 }
    tp: { levels_r: [1.0, 2.0, 3.0], partials: [0.5, 0.3, 0.2] }
    trailing:
      enabled: true
      mode: CHANDELLIER      # CHANDELLIER | ATR_TRAIL | STEP
      k_atr: 2.5
      breakeven_after_r: 1.0
  funding:
    enabled: true
    source: EXCHANGE_RATES   # EXCHANGE_RATES | FIXED_RATE
    fixed_rate_apr: 0.0
```

**Правила конфига**

* Все ссылки (`risk_params_ref`, комиссии/fees, плечо, isolated margin) должны соответствовать записям из `risk_params`/`exchange_params` (M2).
* Конфиг **канонизируется** и регистрируется в `backtest_params` (append‑only). `backtest_run` хранит `params_id` и временной интервал/юнит теста.

---

## Таблицы бэктеста (append‑only)

> Без триггеров, все события иммутабельны. Идемпотентность через уникальные ключи run‑scoped.

### 1) `backtest_params`

| Колонка        | Тип         | Комментарий                                         |
| -------------- | ----------- | --------------------------------------------------- |
| id             | BIGSERIAL   | PK                                                  |
| version        | TEXT        | Версия схемы/логики симулятора                      |
| canonical_json | JSONB       | Зафиксированный снимок конфигурации (канонизирован) |
| created_at     | TIMESTAMPTZ | —                                                   |

**UNIQUE** `(version, canonical_json)`

### 2) `backtest_run`

| Колонка     | Тип                            | Комментарий                  |
| ----------- | ------------------------------ | ---------------------------- |
| id          | BIGSERIAL                      | PK                           |
| params_id   | BIGINT FK→`backtest_params.id` | Ссылка на конфиг             |
| label       | TEXT                           | Человекочитаемое имя запуска |
| from_utc    | TIMESTAMPTZ                    | Окно старта                  |
| to_utc      | TIMESTAMPTZ                    | Окно конца                   |
| created_at  | TIMESTAMPTZ                    | —                            |
| finished_at | TIMESTAMPTZ                    | —                            |

### 3) `sim_order`

| Колонка         | Тип                         | Комментарий                                     |                  |             |            |
| --------------- | --------------------------- | ----------------------------------------------- | ---------------- | ----------- | ---------- |
| id              | BIGSERIAL                   | PK                                              |                  |             |            |
| run_id          | BIGINT FK→`backtest_run.id` | —                                               |                  |             |            |
| submitted_ts    | TIMESTAMPTZ                 | Время подачи (по свече)                         |                  |             |            |
| side            | TEXT                        | BUY                                             | SELL             |             |            |
| type            | TEXT                        | MARKET                                          | LIMIT            | STOP_MARKET | STOP_LIMIT |
| tif             | TEXT                        | GTC                                             | IOC              | FOK         |            |
| qty             | NUMERIC(38,12)              | Количество (контракты/коины)                    |                  |             |            |
| price           | NUMERIC(50,30)              | Цена                                            |                  |             |            |
| stop_price      | NUMERIC(50,30)              | Для стоп‑типов                                  |                  |             |            |
| status          | TEXT                        | NEW                                             | PARTIALLY_FILLED | FILLED      | CANCELED   |
| signal_event_id | BIGINT                      | Ссылка на событие сигнала (если source=SIGNALS) |                  |             |            |
| reason          | JSONB                       | Объяснимость причины выставления                |                  |             |            |

**INDEX** `(run_id, submitted_ts)`

### 4) `sim_fill`

| Колонка      | Тип                      | Комментарий                 |       |
| ------------ | ------------------------ | --------------------------- | ----- |
| id           | BIGSERIAL                | PK                          |       |
| order_id     | BIGINT FK→`sim_order.id` | —                           |       |
| ts           | TIMESTAMPTZ              | Время исполнения            |       |
| price        | NUMERIC(50,30)           | Цена исполнения             |       |
| qty          | NUMERIC(38,12)           | Объём                       |       |
| liquidity    | TEXT                     | MAKER                       | TAKER |
| fee          | NUMERIC(38,12)           | Комиссия (в quote)          |       |
| slippage_bps | NUMERIC(12,6)            | Фактическое проскальзывание |       |

### 5) `sim_position`

| Колонка      | Тип            | Комментарий                                |       |     |      |          |        |
| ------------ | -------------- | ------------------------------------------ | ----- | --- | ---- | -------- | ------ |
| id           | BIGSERIAL      | PK                                         |       |     |      |          |        |
| run_id       | BIGINT         | —                                          |       |     |      |          |        |
| opened_ts    | TIMESTAMPTZ    | —                                          |       |     |      |          |        |
| closed_ts    | TIMESTAMPTZ    | —                                          |       |     |      |          |        |
| side         | TEXT           | LONG                                       | SHORT |     |      |          |        |
| entry_price  | NUMERIC(50,30) | —                                          |       |     |      |          |        |
| exit_price   | NUMERIC(50,30) | —                                          |       |     |      |          |        |
| qty          | NUMERIC(38,12) | —                                          |       |     |      |          |        |
| pnl_quote    | NUMERIC(50,30) | Прибыль в quote                            |       |     |      |          |        |
| pnl_r        | NUMERIC(38,12) | Прибыль в R                                |       |     |      |          |        |
| mae_r        | NUMERIC(38,12) | MAE в R                                    |       |     |      |          |        |
| mfe_r        | NUMERIC(38,12) | MFE в R                                    |       |     |      |          |        |
| bars_held    | INTEGER        | Длительность в барах                       |       |     |      |          |        |
| exit_cause   | TEXT           | TP                                         | SL    | TSL | TIME | OPPOSITE | MANUAL |
| entry_reason | JSONB          | Снимок причины входа (из `reason` сигнала) |       |     |      |          |        |
| exit_reason  | JSONB          | Причина выхода (правило/барьер)            |       |     |      |          |        |

### 6) `sim_trade` (нормализованный вид сделок)

* Можно хранить агрегированно (из `sim_position`) для ускорения отчётов.

### 7) `equity_curve`

| run_id | ts | equity_quote | drawdown_quote |

### 8) `performance_aggregate`

| run_id | n_trades | winrate | expectancy_r | profit_factor | sharpe | sortino | max_drawdown | avg_bars | median_mae_r | median_mfe_r | ... |

> Примечание: метрики считаются **после** комиссий/сллипеджа/funding.

---

## Модель исполнения (bar‑by‑bar)

**Фиделити уровни**

* **L1 (по умолчанию):** intrabar путь `OPEN→(extreme)→(other extreme)→CLOSE` выбирается в зависимости от направления (для long сначала `LOW`, для short сначала `HIGH`). Опции: `OPEN‑HIGH‑LOW‑CLOSE`/`OPEN‑LOW‑HIGH‑CLOSE`.
* **L2:** использование mid/bid/ask и фиксированного спреда/модели FIXED_BPS.
* **L3:** кастомные правила частичных исполнений, очередности и проскальзывания.

**Заполнение MARKET**

* Исполняется на следующем баре (latency_bars) по `OPEN` ± slippage.

**LIMIT**

* Считается исполненным, если intrabar диапазон включает цену лимита (возможны частичные исполнения по модели L2–L3).

**STOP_MARKET / STOP_LIMIT**

* Триггер на касание `stop_price` (по диапазону бара), далее исполнение согласно типу.

**TIF**

* `IOC` — заполняется частично в пределах бара, остаток отменяется; `FOK` — либо полный fill, либо отмена; `GTC` — переносится на следующий бар.

---

## Риск‑менеджмент и позиционирование

* **Isolated margin, плечо ≤ x10**, риск **≤ 1%** депозита на сделку (берём `account_equity` на момент входа).
* Размер позиции `qty` вычисляем от дистанции до SL (в цене) и допустимого риска в quote.
* Ограничения: `max_concurrent_positions`, `pyramiding` (true/false), `opposite_signal_exit`.

**Stops/Targets**

* **SL**: фиксированный (в R или ATR k), либо **trailing** (ATR/Chandelier/ступенчатый).
* **Перевод в BE**: при достижении `breakeven_after_r` перенос SL в цену входа.
* **TP partials**: массив долей `partials` для закрытия части объёма на уровнях `levels_r`.

---

## Комиссии, проскальзывание, funding

* **Комиссии**: `fee = price * qty * fee_bps / 10000` по роли (maker/taker).
* **Проскальзывание**: baseline `slippage_bps` применяем к цене исполнения (плюс/минус относительно стороны).
* **Спред**: модель `MID`/`FIXED_BPS`/`LAST` влияет на цену LIMIT/STOP и заполнение.
* **Funding**: если `enabled`, периодически списываем/начисляем согласно ставкам (если ставок нет — используем `fixed_rate_apr`/суточный эквивалент).

Все величины учитываются в `pnl_quote`/`pnl_r` и кривой equity.

---

## Алгоритм симуляции (псевдокод)

```
load universe bars (ASC)
load signal events (if source=SIGNALS)
state := {orders, positions, equity, pendingLatencyQueue}
for each bar in window:
  materialize signals with latency_bars → submit orders
  move GTC/STOP orders across bars; cancel IOC/FOK as needed
  simulate fills by type & model (L1..L3) using bar[open,high,low,close]
  update positions (avg price, qty), compute SL/TP/trailing/BE checks
  close positions on events (TP/SL/TSL/TIME/OPPOSITE)
  apply fees/slippage/funding
  append equity point; journal orders/fills/positions/trades
end
compute aggregates & metrics
```

---

## REST API (черновик)

* `POST /api/backtest/run` — тело: YAML/JSON конфигурации (или ссылка на `backtest_params_ref`); ответ: `runId`.
* `GET  /api/backtest/runs` — список запусков и статусы.
* `GET  /api/backtest/{runId}/equity` — кривая equity (downsample для UI).
* `GET  /api/backtest/{runId}/trades` — сделки с причинами входа/выхода.
* `GET  /api/backtest/{runId}/metrics` — сводные метрики/таблицы.
* `DELETE /api/backtest/{runId}` — (dev‑режим) очистка артефактов запуска.

---

## Метрики и отчёты

**Per‑run:**

* `n_trades`, `winrate`, `expectancy_r`, `profit_factor`, `avg_win/avg_loss`, `CAGR`, `Sharpe`, `Sortino`, `MaxDD`, `avg_bars_held`, `exposure`, `skew/kurtosis` P&L.
* Разрезы: по **типам сигналов**, **канонам ТФ**, **|score|‑бакам**, **режимам рынка** (ATR‑квантили, тренд/флэт), дням недели/сессиям.

**Prometheus (runtime симулятора):**

* `backtest_orders_total{type,role}`
* `backtest_fills_total{liquidity}`
* `backtest_equity_update_ms`
* `backtest_trades_total{exitCause}`

---

## Explainability

* Каждая запись `sim_order` содержит `signal_event_id` и snapshot `reason`.
* `sim_position.entry_reason/exit_reason` фиксируют правила, приведшие к действию (например, `{"tp": 2.0, "partial": 0.5}`).
* В отчётах указывать примеры сделок с расшифровкой компонентов `score`.

---

## Конкуренция и детерминизм

* Один поток на **run × серия** для гарантии детерминизма; параллельные ран‑шарды — по сериям/инструментам.
* Все источники данных иммутабельны; никаких внешних побочных эффектов.
* Отключить случайность: **никаких** RNG в моделях по умолчанию; все эвристики — детерминированы.

---

## Definition of Done (M9)

* [ ] `backtest_params` и `backtest_run` реализованы; конфиг канонизируется и сохраняется (append‑only).
* [ ] Симулятор бар‑за‑баром поддерживает MARKET/LIMIT/STOP*, TIF, latency, частичные исполнения.
* [ ] Реализовано позиционирование по риску (≤1% на сделку), isolated margin и плечо ≤ x10.
* [ ] Поддержаны SL/TP, trailing, безубыток, partials; opposite‑signal exit (опционально).
* [ ] Учтены комиссии, slippage, funding; кривая equity/журналы/метрики формируются.
* [ ] REST для запуска/просмотра результатов; метрики Prometheus.
* [ ] Полная объяснимость причин входа/выхода/закрытия.
* [ ] Детерминизм результатов при одинаковой конфигурации и данных.

---

## Приложение A — Формулы

* **R**: `R = |entry_price − sl_price|` в ценовых единицах; `pnl_R = pnl_quote / (R * qty)` (знак по направлению).
* **Sharpe**: `mean(returns)/stdev(returns) * sqrt(252)` (или по выбранной частоте).
* **Sortino**: `mean(returns)/stdev(min(returns,0)) * sqrt(252)`.
* **Profit Factor**: `Σ win / Σ loss`.
* **Drawdown**: по `equity_curve` с пиками/просадками.

---

## Приложение B — Калибровка и сценарии

* Серии «sanity check»: константная цена (без сделок), монотонный тренд (ожидаемые выигрыши для тренд‑правил), пилообразный ряд (зона повышенных ложных входов).
* Проверка инвариантов: отсутствие look‑ahead, корректность time‑ordering, устойчивость к дыркам (после M5 их быть не должно).
