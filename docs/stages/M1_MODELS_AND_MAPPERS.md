# M1_MODELS_AND_MAPPERS.md — Доменные модели и маппинг (4-уровневая схема)

Версия: 1.1 • Цель — зафиксировать **доменную модель** и правила **маппинга** между слоями: persistence (jOOQ), domain (rich), client (биржи), api (REST DTO). Реализация без бизнес-логики индикаторов/сигналов (они позже).

> На этом этапе **сознательно исключаем тестирование** — оно будет отдельным этапом.

---

## 0) Контекст и зависимости
- Стек: Java 25, Spring Boot 3.5.x, **jOOQ-only** (без JPA), MapStruct, Flyway, Spring Security (JWT), springdoc.
- Источник правды — README и TECH_STYLE_GUIDE (jOOQ-only). Таймфреймы — **только** `com.example.tradingbot.util.OkxTimeframes.*` (регистрозависимо).
- Доменные правила:
    - **Domain — единственное связующее звено**. Любые преобразования идут через Domain.
    - Иммутабельные реестры параметров (`*_params`) — **append-only**.
    - Часовой пояс — **UTC**. Числа — `BigDecimal` с явным округлением.
    - Стиль: по одной переменной в строке; всегда `{}` после `if/for/while`.

---

## 1) 4 уровня моделей и маршрут данных

client (биржа) → domain ← api (REST)
↑
persistence (jOOQ)

markdown
Копировать код

**Маршрут строго через domain.** Запрещены прямые client→api, client→persistence, api→persistence, client↔client, api↔api.

### 1.1 Слои
- **persistence** — jOOQ generated POJO/Record + наши DAO/репозитории (только SQL и маппинг).
- **domain** — «rich model»: инварианты, фабрики, value-объекты, методы поведения.
- **client** — модели конкретной биржи и адаптеры (анти-коррупционный слой).
- **api** — DTO запросов/ответов (версионирование `api.v1`), без бизнес-логики.

---

## 2) Перечень доменных сущностей (минимум M1)

> Слои индикаторов/сигналов появятся в M6–M8; сейчас — фундамент.

### 2.1 Основные
- **Instrument** — `id`, `name`, `base`, `quote`, `priceStep`, `qtyStep`, `minNotional`, `isPerpetual`.
- **Exchange** — `id`, `name`, базовые комиссии/лимиты/funding-правила.
- **HistoryGroup** — связка (Instrument × Timeframe): `id`, `instrumentId`, `timeframe`, `createdAt`, `coverageStartUtc`.
- **Candle** (domain) — `historyGroupId`, `timestampUtc`, `open/high/low/close`, `volumeCoin`, `volumeCurrency`, `status`.

### 2.2 Параметры (immutable registries)
- **IndicatorParams**, **SignalParams**, **QuorumParams**, **RiskParams**, **ExchangeParams**:
    - поля: `id`, `type/indicator`, `timeframe (OkxTimeframes.*)`, `version`, `canonical_json`, `created_at`, `created_by`, `is_active`
    - ключ: `UNIQUE(type,timeframe,version,canonical_json)`
    - только insert (append-only), без update/delete.

### 2.3 Технические
- **IndicatorCheckpoint** — `(indicator, timeframe, indicator_params_id, version) → last_ts` (монотонно растёт).
- **ExchangeSymbolMapping** — соответствия символов бирж доменному `Instrument`.

---

## 3) Правила доменной модели (rich model)
- **Инварианты** в фабриках/конструкторах (цены ≥ 0 и т.п.).
- **Value-objects**: `Timeframe` (обёртка над OkxTimeframes), `Money`, `Quantity`, `Percent`, `TimestampUtc`.
- **Методы поведения**: напр. `Candle.mid()`, `Instrument.roundPrice(raw)`, `roundQty(raw)`.
- **Domain** не знает про SQL/HTTP — только бизнес-семантика.

---

## 4) Мапперы (MapStruct) и контракты

Интерфейсы:
- `ClientToDomainMapper` — client → domain
- `DomainToClientMapper` — domain → client (редко)
- `ApiToDomainMapper` — api.requests → domain
- `DomainToApiMapper` — domain → api.responses
- `PersistenceToDomainMapper` — jOOQ POJO/Record → domain
- `DomainToPersistenceMapper` — domain → jOOQ POJO

Правила:
- `unmappedTargetPolicy = ERROR`, `nullValueCheckStrategy = ALWAYS`, `componentModel = spring`.
- Временные метки — всегда UTC; приводить в мапперах явно.
- **Timeframe**: только `OkxTimeframes.*`/`Timeframe` (никаких «1d/1D» строк).
- Денежные/объемы — `BigDecimal` (никаких `double` в домене).
- Для `*_params` — работать по `id` и `canonical_json` (транзиентные настройки не копировать).

---

## 5) Persistence (jOOQ) ↔ Domain
- Репозитории принимают/возвращают **domain**, а внутри конвертируют через мапперы к jOOQ POJO/Record.
- В M1 поддерживаем CRUD базовых сущностей:
    - `InstrumentRepository`: `findById`, `findByName`, `saveIfAbsent`, `listAll()`
    - `HistoryGroupRepository`: `upsert(...)`, `findByInstrumentAndTf(...)`
    - `ParamsRegistry`: `resolveOrCreate(IndicatorParams cfg) → id` (канонизация JSON → поиск UNIQUE → insert при отсутствии)
- Идемпотентность — за счёт `UNIQUE` + `INSERT ... ON CONFLICT`.

---

## 6) Client (биржи) ↔ Domain
- Разные биржи = различные client-модели; приводятся к единой доменной форме в `ClientToDomainMapper`.
- Минимальный набор на M1: клиентские `Candle/CandleResponse`, справочники символов, базовая ошибка API.

---

## 7) API (REST) ↔ Domain
- Версионирование: `api.v1`.
- DTO — плоские, без доменных методов.
- Доступ — через Security (JWT). `/v3/api-docs`/`/swagger-ui` — по профилю/политике.
- На M1 только DTO для вспомогательных операций (регистрация инструмента, чтение истории-метаданных, резолв параметров).

---

## 8) Конструкторный скелет пакетов
```
com.example.tradingbot
├─ api/v1/
│ ├─ dto/
│ │ ├─ InstrumentDto.java
│ │ ├─ ExchangeDto.java
│ │ └─ ...
│ └─ controller/
│ └─ InstrumentController.java
├─ client/
│ ├─ okx/
│ │ ├─ model/ # CandleView, CandleResponse
│ │ └─ OkxClient.java
│ └─ ... (другие биржи)
├─ domain/
│ ├─ model/
│ │ ├─ Instrument.java
│ │ ├─ Exchange.java
│ │ ├─ HistoryGroup.java
│ │ ├─ Candle.java
│ │ ├─ params/
│ │ │ ├─ IndicatorParams.java
│ │ │ ├─ SignalParams.java
│ │ │ ├─ QuorumParams.java
│ │ │ ├─ RiskParams.java
│ │ │ └─ ExchangeParams.java
│ │ └─ tech/IndicatorCheckpoint.java
│ ├─ service/
│ │ ├─ InstrumentService.java
│ │ └─ ParamsRegistryService.java
│ └─ value/
│ ├─ Timeframe.java
│ ├─ Money.java
│ ├─ Quantity.java
│ └─ TimestampUtc.java
├─ mapping/
│ ├─ ClientToDomainMapper.java
│ ├─ DomainToClientMapper.java
│ ├─ ApiToDomainMapper.java
│ ├─ DomainToApiMapper.java
│ ├─ PersistenceToDomainMapper.java
│ └─ DomainToPersistenceMapper.java
├─ persistence/
│ ├─ jooq/generated/... # (в .gitignore)
│ └─ repo/
│ ├─ InstrumentRepository.java
│ ├─ HistoryGroupRepository.java
│ └─ ParamsRegistry.java
└─ util/
└─ OkxTimeframes.java
```
---

## 9) Примеры контрактов (скелеты, без реализации)

### Instrument (domain)

```java
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Instrument {
    private long id;
    private String name;
    private String base;
    private String quote;
    private BigDecimal priceStep;
    private BigDecimal qtyStep;
    private boolean perpetual;
}
```
MapStruct (пример)
```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PersistenceToDomainMapper {
  Instrument toDomain(InstrumentPojo pojo);
  // ...
}
```
10) Definition of Done (M1)

- [ ] Созданы доменные классы из §2 (скелеты + инварианты/фабрики).
- [ ] Добавлены интерфейсы мапперов (§4) с базовой конфигурацией MapStruct.
- [ ] Подготовлены репозитории и их контракты (§5) — без бизнес-логики.
- [ ] Добавлены DTO для API (§7) и каркас контроллеров.
- [ ] В репозитории есть структура пакетов (§8); generated не в VCS.
- [ ] Все даты в UTC, без double в домене, TF — только OkxTimeframes.*.
