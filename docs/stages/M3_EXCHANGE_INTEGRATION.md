# M3 — Интеграция с биржами (данные)

**Цель этапа.** Подключить биржи и зафиксировать **единый контракт (SPI)** для работы с рыночными данными и метаданными: канонические таймфреймы, маппинг символов, аутентификация/подписи, лимиты/ретраи, наблюдаемость. **Загрузка истории** (бэкфилл/инкремент/докачка) выполнится на следующем этапе (M4).

> Мы строго следуем структуре этапов из главного README: M3 = интеграция, M4 = загрузка истории, M5 = целостность. В этом документе нет требований по записи в БД и оркестрации бэкфилла — только контракт и коннекторы.

---

## Scope / Out of Scope

**Входит в M3:**

* Общий **Exchange SPI** (интерфейсы) для:

    * получения **закрытых свечей** (методы/сигнатуры без реализации пайплайна),
    * чтения биржевых **метаданных** (список символов, шаги округления, комиссии/фандинг — по мере необходимости),
    * обслуживания **времени биржи** (для подписи, анти‑дрейф),
    * трансляции **канонических таймфреймов** → коды биржи.
* Коннектор **OKX** как референс; каркас для добавления других бирж.
* **Аутентификация и подписи**, требования к секретам и безопасным настройкам.
* **Rate‑limit / Retry / Timeout** (через resilience4j), классификация ошибок.
* **Наблюдаемость**: логи, метрики, трассировки; контракты health‑проверок.

**Не входит (переносится в M4/M5):**

* Загрузка/запись свечей в БД, бэкфилл/инкремент, докачка «дыр».
* Расчёт индикаторов/сигналов и всё, что выше по конвейеру.

---

## Архитектура и SPI

### 1) Идентификаторы и домен

* `ExchangeId` — перечисление/тип: `okx`, `binance`, ...
* **Слои моделей (принцип):** `client ↔ domain ↔ persistence ↔ api`. Любая интеграция идёт через **доменную** модель; прямые маппинги client→persistence/api — **запрещены**.

### 2) Контракты (интерфейсы)

* `ExchangeConnector` (SPI):

    * `ExchangeId id()` — идентификатор биржи.
    * `ExchangeClock clock()` — работа со временем биржи (см. ниже).
    * `SymbolBook listSymbols()` — кэшируемый справочник торгуемых инструментов (включая шаги округления и статус доступности).
    * `ExchangeSymbol resolve(Instrument instrument)` — маппинг доменного инструмента в биржевой символ/тип контракта.
    * `List<ClientCandle> loadClosedCandles(ExchangeSymbol symbol, String exchangeTfCode, Instant beforeExclusive, int limit)` — **чтение закрытых свечей** (пагинация назад по времени). *Реализация пайплайна и запись в БД — в M4.*
* `TimeframeTranslator`:

    * `String toExchangeCode(ExchangeId id, String timeframeCanonical)` — из каноники → в код биржи.
    * `String fromExchangeCode(ExchangeId id, String code)` — обратная трансляция (для валидаций/логов).
* `ExchangeClock`:

    * `Duration skew()` — оценка дрейфа локального времени относительно биржевого.
    * `Instant nowExchange()` — текущее время биржи (для подписей/таймстампов).

### 3) Клиентские модели (минимальный обязательный состав)

* `ClientCandle { ts, open, high, low, close, volumeBase?, volumeQuote? }` — **всегда закрытая** свеча.
* `ExchangeSymbol { clientSymbol, contractType, marginMode?, leverageMax? }` — представление символа биржи.
* `SymbolBook { List<ExchangeSymbol> symbols, Rounding/filters }` — каталог символов + шаги цены/количества.

### 4) Маппинг client→domain

* MapStruct‑мапперы: `ClientCandle → domain.Candle`, `ExchangeSymbol → domain.ExchangeInstrumentDescriptor`.
* Доменная модель — **единый узел** маппингов: client↔domain↔persistence↔api.

---

## Канонические таймфреймы и конфиг

### 1) YAML (пример)

```yaml
app:
  timeframes:
    canonical: ["1 minute","3 minutes","5 minutes","15 minutes","1 hour","2 hours","4 hours","1 day"]

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

# пример другой биржи (скелет)
binance:
  base-url: https://api.binance.com
  timeframes:
    "1 minute": "1m"
    "1 hour":   "1h"
    "1 day":    "1d"
```

### 2) Валидации

* При старте: для **каждой активной биржи** все `app.timeframes.canonical` должны иметь соответствие в `<exchange>.timeframes` (1:1). Иначе — fail‑fast.
* `TimeframeTranslator` строит обратные словари (код биржи → каноника) — для диагностики и журналирования.

---

## Аутентификация, подписи, время

### 1) Секреты

* Хранить ключи/пароли в Secret Manager/ENV; в YAML — только плейсхолдеры `${…}`.
* Конфигурация коннектора — отдельный бин с `@ConfigurationProperties` и **без дефолтов в коде**.

### 2) Подписи (пример: OKX REST)

* Заголовки: `OK-ACCESS-KEY`, `OK-ACCESS-SIGN`, `OK-ACCESS-TIMESTAMP`, `OK-ACCESS-PASSPHRASE`.
* Сообщение для HMAC‑SHA256: `timestamp + method + path + (body|"")`; затем Base64.
* Временная метка должна учитывать `ExchangeClock.skew()`; при сильном дрейфе — синхронизироваться.

### 3) Анти‑дрейф времени

* Эндпойнт времени биржи (если есть) опрашивается периодически; храним скользящую оценку `skew()`.
* В логах фиксируем отклонение и используем его в подписях.

---

## Rate‑limit, Retry, Timeout (без `Thread.sleep()`)

* `resilience4j`:

    * `RateLimiter` — контроль rps/квот на биржу/эндпойнт.
    * `Retry` — только на **транзиентные** ошибки (сетевые, 5xx, таймауты); backoff = экспоненциальный.
    * `TimeLimiter` — жёсткие таймауты вызовов.
    * `CircuitBreaker` — защита от деградации.
* Классификация ошибок: 4xx (валидируем конфиг/запрос), 5xx/timeout (ретраи), бизнес‑ошибки (например, «rate limit exceeded») — с паузой из заголовков.
* Межбатчевые ожидания делать через `RateLimiter`/планировщик, а не `Thread.sleep()`.

---

## Наблюдаемость

* **Логи** (structured): запрос/ответ (без чувствительных данных), latency, попытки/ретраи, заголовки rate‑limit.
* **Метрики** Prometheus: `exchange_api_requests_total{exchange,endpoint,status}`, `exchange_api_latency_ms{…}`, `exchange_rate_limit_wait_ms{…}`, `exchange_retry_total{…}`.
* **Трейсинг** (опционально): OpenTelemetry с атрибутами `exchange`, `endpoint`, `tf`, `symbol`.

---

## Health‑проверки и сервисные REST‑эндпойнты (для DevOps/QA)

* `GET /api/exchanges/{id}/ping` — доступность REST.
* `GET /api/exchanges/{id}/time` — серверное время и оценка `skew`.
* `GET /api/exchanges/{id}/symbols` — текущий `SymbolBook` (в т.ч. шаги округления/статус доступности).
* `POST /api/exchanges/{id}/resolve` — маппинг доменного `Instrument{name,base,quote}` → `ExchangeSymbol{clientSymbol,…}`.
* `GET /api/exchanges/{id}/timeframes/validate` — результат проверки полноты маппингов таймфреймов.

> Эти эндпойнты — **диагностические**. Они не запускают запись данных. Пайплайн бэкфилла будет в M4.

---

## Требования к качеству и безопасности

* Коннекторы **статлес**; многопоточность — через thread‑safe клиенты HTTP и независимые RateLimiter’ы на `(exchange, endpoint)`.
* Все значения времени — UTC; явные границы выборок в сигнатурах (`beforeExclusive`).
* Явная сериализация чисел (String→BigDecimal с заданным `MathContext`).
* Секреты не попадают в логи; sensitive‑флаги в маскировании логов/трейсов.

---

## Алгоритм добавления новой биржи

1. Добавить секцию `<exchangeId>` в YAML (`base-url`, `timeframes`, auth‑поля/лимиты).
2. Реализовать `ExchangeConnector` под биржу (REST‑клиент, подписи, `listSymbols`, `loadClosedCandles`).
3. Зарегистрировать `TimeframeTranslator` для биржи.
4. Включить health‑эндпойнты; проверить `timeframes/validate`, `ping`, `time`, `symbols`.
5. Обновить `exchange_params` (M2) — комиссии/фандинг/шаги округления, если нужны для последующих этапов.

---

## Definition of Done (M3)

* [ ] Единые интерфейсы `ExchangeConnector`, `TimeframeTranslator`, `ExchangeClock` задокументированы и реализованы для OKX.
* [ ] Конфигурации YAML: каноника таймфреймов + маппинги `<exchange>.timeframes`; валидация на старте.
* [ ] Аутентификация/подписи реализованы (OKX); секьюрное хранение секретов.
* [ ] Встроенные ограничители `RateLimiter/Retry/Timeout/CircuitBreaker` с настраиваемыми квотами.
* [ ] Логи/метрики/трейсинг для всех вызовов биржи.
* [ ] Диагностические REST‑эндпойнты (`ping/time/symbols/resolve/validate`) доступны.
* [ ] Документация по добавлению новой биржи.

---
