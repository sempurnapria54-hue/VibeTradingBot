# M11 — LIVE: коннекторы ордеров, управление позициями, риск

**Цель этапа.** Запустить **реальную торговлю** по решениям кворума (M10) на биржах (M3), строго соблюдая: **isolated margin**, **плечо ≤ x10**, **риск ≤ 1% депозита на сделку**, блокировку торговли при серии убыточных сделок, полную **объяснимость** цепочки *значения индикаторов → сигналы → кворум → решение → ордера/позиции* и **идемпотентность** всех действий.

> Важные принципы: только **закрытые свечи** как триггеры действий; коннекторы — через унифицированный SPI; **append‑only** журналы; без триггеров в БД; предпочтительно **без UPDATE** (снимки + журналы) или с **оптимистичными блокировками**, если нужен апдейт снапшотов.

---

## Зависимости

* **M2**: `quorum_params`, реестры `risk_params`/`exchange_params`, базовые сущности (`exchange`, `instrument`, `exchange_instrument`), индексы.
* **M3**: коннекторы к биржам (REST/WebSocket), трансляция таймфреймов, `ExchangeClock`.
* **M4–M5**: непрерывная история свечей (для ATR/SL/TP вычислений на закрытом баре).
* **M6–M7**: значения индикаторов (ATR, BB и т. п. для стопов/трейлинга).
* **M8**: `signal_event` (для explainability/диагностики).
* **M9**: параметры исполнения/риска (как источник дефолтов), модели slippage/fees.
* **M10**: `quorum_decision_event` (что делать, когда и почему) + `recommendations`.

---

## Scope / Out of Scope

**Входит:**

* LIVE‑ордеринг: выставление/модификация/отмена ордеров (market/limit/stop), постановка и обслуживание **защитных** (SL/TP), **trailing**, перевод в **безубыток**, **partials**.
* Состояния позиций: открытие/увеличение/уменьшение, закрытие, журналирование fill’ов.
* Риск/guardrails: контроль лимитов плеча, маржи, **≤1%** на сделку, **pause‑switch** при серии убытков.
* Реконсиляция: сверка локальных журналов с биржей; авто‑исправление рассинхронов.
* Объяснимость: связки `quorum_decision_event → live_order → fills → position_state`.
* REST/метрики/алерты; учёт funding (при наличии данных/биржевых событий).

**Не входит:**

* Бэктест (M9) — используется как справочник параметров; генерация сигналов/кворума — в M8/M10.

---

## Архитектура рантайма

**Компоненты:**

1. **LiveSessionOrchestrator** — управляет жизненным циклом live‑сессии (start/stop, health, graceful shutdown). Один сеанс на `exchange_instrument × timeframe_canonical`.
2. **DecisionSubscriber** — подписка на `quorum_decision_event` (M10) по TF; триггерит ордеринг на закрытии бара.
3. **OrderRouter** — перевод решения в набор клиентских команд (entry/SL/TP/modify/cancel) с учётом `recommendations`/risk.
4. **RiskEngine** — применяет `risk_params` (≤1% на сделку, плечо ≤ x10, isolated), лимиты на кол-во позиций, **loss‑streak guard**.
5. **ExchangeConnector (LIVE)** — SPI с адаптерами бирж (OKX, …): place/cancel/amend, fetch‑orders, fetch‑fills, fetch‑positions, set‑leverage/marginMode.
6. **Reconciler** — периодически сверяет open‑orders/positions с биржей, чинит рассинхроны.
7. **Ledger** — *append‑only* журналы: intents, orders, fills, funding, guardrails. Снапшоты `position_state` (опционально с оптимистичным lockVersion).

**Потоки и конкурентность:**

* «Один писатель на позицию»: ключ `(exchangeId, exchangeInstrumentId)`.
* Очередь команд идемпотентна по `client_order_id`.
* Внешние вызовы через rate‑limiter и retry‑политику без `Thread.sleep()` (пейсинг через планировщик/лимитер).

---

## Жизненный цикл решения → ордера

1. Приходит `quorum_decision_event` на закрытии бара TF кворума.
2. `RiskEngine` проверяет лимиты (плечо, изолированная маржа, размер позиции от 1% риска на SL‑дистанцию, **loss‑streak**/pause).
3. `OrderRouter` формирует `OrderIntent`(ы):

    * `ENTER_*` → заявку на вход + постановку защитных (SL/TP) в виде связанного *брекета*;
    * `MODIFY_SLTP` → модификации стопов/таргетов (в т. ч. трейлинг/BE);
    * `EXIT/REVERSE` → рыночное/лимитное закрытие и зачистка защитных.
4. `ExchangeConnector` выставляет/меняет/отменяет ордера, возвращает `external_order_id`.
5. Через WebSocket/REST‑polling приходят `fills` → журналируем и обновляем `position_state`.
6. `Reconciler` сверяет локальные open‑orders/позиции с биржей (периодически и при ошибках).

---

## Модель данных (LIVE, append‑only + минимальные снапшоты)

> Без триггеров. Идемпотентность: везде **уникальные ключи** на run‑scope/`client_order_id`/`external_order_id`.

### 1) `live_session`

* `id PK`, `exchange_id FK`, `exchange_instrument_id FK`, `timeframe_canonical TEXT`, `status (RUNNING/PAUSED/STOPPED)`, `started_at`, `stopped_at`, `reason TEXT`.
* **UNIQUE** `(exchange_instrument_id, timeframe_canonical)` при `status in (RUNNING,PAUSED)` — одна активная сессия.

### 2) `order_intent`

* Журнал внутренних намерений.
* Поля: `id PK`, `session_id FK`, `quorum_decision_id FK`, `type (ENTER/EXIT/REVERSE/MODIFY_SLTP/CANCEL)`, `side`, `qty`, `price`, `sl`, `tp[]`, `trailing_json`, `client_order_id TEXT`, `created_at`.
* **UNIQUE** `(client_order_id)` — детерминированный хеш из `(strategy, session, candle_ts, action)`.

### 3) `live_order`

* Фактические ордера, как на бирже.
* Поля: `id PK`, `intent_id FK`, `external_order_id TEXT`, `type`, `side`, `status (NEW/PARTIALLY_FILLED/FILLED/CANCELED/REJECTED)`, `price`, `stop_price`, `tif`, `reason JSONB`, `created_at`.
* **UNIQUE** `(external_order_id)`; индекс `(intent_id)`.

### 4) `live_fill`

* Филлы/исполнения.
* Поля: `id PK`, `order_id FK`, `ts`, `price`, `qty`, `liquidity (MAKER/TAKER)`, `fee_quote`, `slippage_bps`, `raw JSONB`.

### 5) `position_ledger`

* События позиции (event‑sourced): `OPEN/ADD/REDUCE/CLOSE/SL/TP/TSL/BE/FUNDING`.
* Поля: `id PK`, `session_id FK`, `ts`, `event_type`, `side`, `qty_delta`, `price_ref`, `pnl_quote_delta`, `pnl_r_delta`, `meta JSONB`.

### 6) `position_state` (снапшот — допускается UPDATE с оптимистичной блокировкой)

* Поля: `id PK`, `session_id FK`, `side (NONE/LONG/SHORT)`, `qty`, `entry_price`, `unrealized_pnl_quote`, `risk_r`, `lock_version BIGINT`, `updated_at`.
* **UNIQUE** `(session_id)`.
* Апдейт через `WHERE lock_version = ?` → `lock_version + 1`.

### 7) `guardrail_event`

* Поля: `id PK`, `session_id FK`, `ts`, `type (LOSS_STREAK_PAUSE/RISK_LIMIT/HEALTH_STOP)`, `params JSONB`, `activated BOOLEAN`.

### 8) `funding_event`

* Если биржа даёт ставки — журналировать начисления/списания funding.

---

## Риск и guardrails

* **Isolated margin** и **плечо ≤ x10** — проверять/устанавливать через коннектор (`setIsolatedMargin`, `setLeverage`). Ошибки → блокировать вход.
* **Риск ≤ 1% на сделку**: `qty` вычисляется как `risk_quote / (distance_to_SL * price_multiplier)`; `risk_quote = equity * 0.01`.
* **Loss‑streak pause**: помнить счётчик убыточных сделок подряд (из `position_ledger`). При достижении порога → писать `guardrail_event` и устанавливать `live_session.status=PAUSED` (или отдельный флаг), блокируя новые `ENTER*` до ручного `RESUME`.
* **Max concurrent positions** по сессии/всей системе.
* **Kill‑switch**: ручная остановка сессии с отменой заявок и безопасным закрытием позиции (опция).

---

## Постановка защитных и трейлинг

* **Bracket**: сразу после входа ставим SL/TP набор (по `recommendations`).
* **Trailing**: периодический пересчёт уровней (по закрытым барам) и `amend` соответствующих ордеров.
* **Breakeven**: при достижении `≥ breakeven_after_r` — передвинуть SL в цену входа.
* **Partials**: при исполнении TP1/TP2 — фиксация доли `qty`, корректировка остатка, сдвиг SL (по политике).

---

## Реконсиляция (self‑healing)

* Периодически: `fetchOpenOrders`/`fetchPositions` → сверить с локальным состоянием.
* Несогласованные ордера:

    * отсутствуют локально, но есть на бирже → либо принять (привязать к intent), либо отменить по политике;
    * есть локально, но нет на бирже → пометить `CANCELED` и пересчитать позиции.
* Филлы, пропущенные из‑за сетевых разрывов, подтягиваются через историю `fetchFills(since)`.

---

## REST API (черновик)

* `POST /api/live/session/start` — `{ exchange, instrument, timeframe, quorumVersion, riskRef }` → `sessionId`.
* `POST /api/live/session/stop` — `{ sessionId, mode: SAFE_CLOSE | CANCEL_ALL }`.
* `POST /api/live/session/pause` / `POST /api/live/session/resume` — управление паузой.
* `GET  /api/live/session/status?sessionId=…` — состояние, открытые ордера/позиция, guardrails.
* `POST /api/live/order/manual` — (dev) ручные заявки внутри сессии.
* `POST /api/live/reconcile/run` — форсировать реконсиляцию.

---

## Метрики и алерты

**Prometheus:**

* `live_orders_total{type,status}`
* `live_fills_total{liquidity}`
* `live_position_exposure{side}`
* `live_guardrail_trips_total{type}`
* `live_reconcile_drift_total`
* `connector_http_latency_ms{exchange,op}` / `connector_ws_events_total{exchange}`

**Алерты:**

* Дрейф позиционной экспозиции > X;
* Ошибки коннектора (серии 5xx/timeout);
* Частые срабатывания guardrails;
* Отставание по свечам/решениям (> K×Δt).

---

## Безопасность и ключи

* Spring Security (см. TECH_STYLE_GUIDE): роли `ADMIN/OPS/READONLY`.
* Секреты (API‑ключи/пароли) — из внешних секрет‑хранилищ/переменных окружения; **не** хранить в коде/репо.
* Все админ‑операции (start/stop/pause/resume/manual order) требуют `ADMIN`.

---

## Идемпотентность, ретраи, лимиты

* Каждый `OrderIntent` несёт **детерминированный** `client_order_id`; повторная обработка безопасна.
* Ретраи только для транзиентных ошибок (сетевые/5xx/rate‑limit) с экспоненциальным backoff.
* Rate‑limit через токен‑бак/планировщик; **без `Thread.sleep()`**.

---

## Definition of Done (M11)

* [ ] Поднята LIVE‑сессия на выбранной связке; решения кворума конвертируются в ордера.
* [ ] Реализован RiskEngine (≤1% на сделку, isolated/x10, loss‑streak pause, max concurrent positions).
* [ ] Включены bracket SL/TP, trailing, breakeven, partials; реконсиляция и self‑healing.
* [ ] Полный журнал: intents/orders/fills/position_ledger/funding/guardrails; снапшоты `position_state` с оптимистичным lock.
* [ ] REST‑эндпойнты управления и статуса; метрики/алерты; безопасность.
* [ ] Объяснимость: каждая сделка ссылается на `quorum_decision_event` и upstream причины.

---

## Приложение A — Формулы

* **Расчёт `qty` от риска**: `risk_quote = equity * risk_pct`, `distance = |entry − SL|`, `notional = risk_quote / distance`, `qty = notional / contract_size` (учёт лота/шага биржи).
* **Плечо**: `leverage = (notional / margin)` не превышает лимитов; при превышении — уменьшить `qty`.

---

## Приложение B — Протоколы отказов

* Потеря WS‑соединения → fallback на REST‑polling + усиленная реконсиляция.
* REJECT от биржи → лог, алерт, пересчёт intents (возможен re‑route: MARKET вместо LIMIT по политике).
* Длительный rate‑limit → деградация частоты действий/перевод в `PAUSED`.
