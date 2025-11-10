# TECH_STYLE_GUIDE.md — технологии, стиль, соглашения (jOOQ‑only)

Версия: 1.2 • Добавлено: продовая БД через **docker-compose.yml** и **YAML**‑конфигурации Spring (`application.yml`).

---

## 0) Рантайм и базовый стек

- **Java**: 25
- **Spring Boot**: 3.5.x (Spring 6.2.x)
- **DB**: PostgreSQL 15+ (локально через Testcontainers; в проде — контейнер из `docker-compose.yml`)
- **Зависимости**:
  - `spring-boot-starter-web`
  - `spring-boot-starter-security`
  - `spring-boot-devtools` (только `dev`)
  - **jOOQ**: `org.jooq:jooq`, `org.jooq:jooq-meta`, `org.jooq:jooq-codegen`
  - Драйвер: `org.postgresql:postgresql`
  - Миграции: `org.flywaydb:flyway-core`
  - DI/утилиты: Lombok, MapStruct
  - Тесты: JUnit 5, Testcontainers, AssertJ, Mockito, Awaitility
  - Наблюдаемость: Micrometer (+ Prometheus при необходимости)
  - Надёжность (опционально): `resilience4j-spring-boot3`
  - Документация API (опционально): `springdoc-openapi-starter-webmvc-ui`
  - JWT/JOSE (опционально): `spring-security-oauth2-jose`

**Allow‑list**: Caffeine, Jackson `datatype-jsr310`, `jakarta.validation`, `logstash-logback-encoder`.  
**Deny‑list**: JPA/ORM (Hibernate, Spring Data JPA), ModelMapper/Dozer, WebFlux.

---

## 1) Архитектура данных и модели (4 уровня)

**Уровни:**
- **persistence** — jOOQ codegen артефакты (Tables, Records, POJOs) + SQL DAO (репозитории на jOOQ DSL).
- **domain** — «rich model» (методы, инварианты, фабрики) — **единственный хаб**.
- **client** — модели внешних бирж (анти‑коррупционный слой).
- **api** — REST DTO (requests/responses), версионирование по пакету `api.v1`.

**Правило маршрута:** все преобразования идут **через domain**. Никаких client→api, api→persistence, client→persistence и т.п.

**Мапперы (MapStruct + фабрики):**
- `ClientToDomainMapper`, `DomainToClientMapper`
- `ApiToDomainMapper`, `DomainToApiMapper`
- `PersistenceToDomainMapper`, `DomainToPersistenceMapper`

---

## 2) jOOQ codegen и слой доступа

**Codegen (Maven/Gradle)**: генерить `Tables`, `Records`, **POJO** (в `persistence.jooq.generated.*`).  
Рекомендации:
- Включить генерацию `equals/hashCode/toString` у POJO.
- `forcedTypes` для `jsonb` → `String` (или Converter).
- Пакет: `com.example.tradingbot.persistence.jooq.generated`.
- `DSLContext` — через HikariCP, Spring‑бин.

---

## 3) Схема БД, миграции и инварианты

- **Flyway** с подробными комментариями к таблицам/полям.
- `snake_case`, `TIMESTAMP` (UTC), числа — `NUMERIC(50,30)`, метаданные — `jsonb`.
- **Иммутабельные реестры**: `indicator_params`, `signal_params`, `quorum_params`, `risk_params`, `exchange_params` — append‑only, `UNIQUE(type,timeframe,version,canonical_json)`.
- **Значения индикаторов** и **оценки бэктеста** — append‑only, `UNIQUE(candle_id, indicator_params_id, version)`.
- Mutable‑исключения: `indicator_checkpoint`, `order`, `order_fill`, `position_state`.
- Индексы: `instrument_id`, `exchange_id`, `timeframe`, `timestamp`. Партиции по (instrument, timeframe)/датам.

---

## 4) Транзакции, идемпотентность, UPSERT

- Чтение — `@Transactional(readOnly=true)`; запись — узкие транзакции на батч.
- Идемпотентность — `UNIQUE` + UPSERT:
  - `INSERT … ON CONFLICT DO NOTHING`
  - `INSERT … ON CONFLICT(key) DO UPDATE SET …`
- Батчи — `DSLContext.batch()`/серии `insertInto(...).onConflict(...).doNothing()`.
- Конфликты — ловим `DataAccessException`, инкрементируем счётчик дублей, продолжаем.

---

## 5) Оптимистические блокировки и mutable‑модель

`version`‑колонка + паттерн `UPDATE … WHERE id=? AND version=?` → `rows==1` ⇒ OK, иначе retry/backoff.  
Предпочтительно **append‑only** событийный поток + периодические снапшоты.

---

## 6) Конкурентность, параллелизм и блокировки

- **Single‑writer** на (биржа, инструмент, TF).
- Уровень БД: `UNIQUE` + UPSERT.
- **pg_try_advisory_lock(key)** для редких коллизий (ключ = hash(exchange|instrument|tf)).
- Внешние API: rate‑limit + bulkhead (Resilience4j); **без `Thread.sleep()`** — `TaskScheduler`/Limiter.

---

## 7) Время, точность, warm‑up

- Везде **UTC**; на входе конвертируем в UTC.
- `BigDecimal` + `MathContext`/`RoundingMode`; защита от деления на ноль.
- Warm‑up бары помечаем: в сигналы не попадают до прогрева.

---

## 8) Значения индикаторов и проверки (инварианты)

- EMA, MACD (`Histogram = MACD − Signal`), RSI/Стохастик ∈ [0,100], Bollinger (`Upper ≥ Basis ≥ Lower`).
- Синтетика: тренд/флэт/шум; сверка с эталонами (ε‑точность).
- Контракт хранения цементируем в M6 (EMA), масштабируем в M7.

---

## 9) Наблюдаемость и аудит

- Логи: `run_id`, `(exchange,instrument,timeframe)`, диапазоны времени, размер батчей, дубль‑счётчик, latency биржи/БД.
- Метрики (Micrometer): докачка, пустые ответы, ошибки провайдера, конфликты версий, latency.
- Decision/audit: `candle → indicator_value → signal_event → quorum_decision → order` + снапшоты параметров.

---

## 10) Конфигурация и стиль (YAML, не .properties)

- Используем **только YAML**: `src/main/resources/application.yml` и профильные `application-dev.yml`, `application-test.yml`, `application-prod.yml`.
- `@ConfigurationProperties` **без дефолтов** — значения приходят из YAML/ENV.
- Таймфреймы — только `OkxTimeframes.*` (регистрозависимо).
- Стиль кода: не объявлять несколько переменных в строке; всегда `{}` после `if/for/while`; бизнес‑методы — в **domain**.

**Пример `application.yml` (база — из ENV):**
```yaml
server:
  port: 8080

spring:
  application:
    name: trading-bot
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  flyway:
    enabled: true
    locations: classpath:db/migration
  jooq:
    sql-dialect: POSTGRES
logging:
  level:
    com.example.tradingbot: DEBUG

security:
  jwt:
    issuer: "trading-bot"
    access-token-ttl: PT15M
    refresh-token-ttl: P14D
```

**Пример `application-prod.yml` (для compose‑сети):**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://db:5432/tradingbot
    username: ${DB_USER}
    password: ${DB_PASSWORD}
logging:
  level:
    root: INFO
```

---

## 11) Продовая БД через docker-compose.yml (обязательно)

- Продовая PostgreSQL разворачивается **через docker-compose.yml**. Секреты и пароли — через ENV/**.env**, либо Docker Secrets. Бэкап/восстановление — отдельными сервисами/джобами.
- `docker-compose.yml` хранится в корне репо; для локалки допустим отдельный `docker-compose.override.yml`.

**Минимальный пример `docker-compose.yml`:**
```yaml
version: "3.9"
services:
  db:
    image: postgres:15-alpine
    container_name: tradingbot-db
    restart: unless-stopped
    environment:
      POSTGRES_DB: tradingbot
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      PGDATA: /var/lib/postgresql/data/pgdata
    volumes:
      - dbdata:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $$POSTGRES_USER -d $$POSTGRES_DB"]
      interval: 10s
      timeout: 5s
      retries: 5
volumes:
  dbdata:
```

**Рекомендации:**
- Не коммитить реальные секреты: использовать `.env` (не в VCS) или Docker Secrets.
- Приложение подключается по `jdbc:postgresql://db:5432/tradingbot` в профиле `prod`.
- Настроить мониторинг БД (pg_exporter/Prometheus) — опционально.

---

## 12) REST API и валидация

- DTO версионируются (`api.v1`), Bean Validation на DTO.
- Единый формат ошибок (код/сообщение/детали).
- Контроллеры тонкие: DTO → domain → сервис → response DTO.

---

## 13) Security & Authentication (Spring Security)

**Stateless JWT** (access + refresh), роли **ROLE_ADMIN/ROLE_TRADER/ROLE_READER**.  
Опции: встроенный `/auth/login` + `/auth/refresh` **или** OAuth2 Resource Server (JWKS).  
Общее: `SessionCreationPolicy.STATELESS`, CORS явный, CSRF off (для REST), ключи RSA/EC в Secret Manager, ротация `kid`, BCrypt ≥ 12.

---

## 14) Тестирование

- Unit: парсеры, индикаторы, мапперы.
- Integration: Testcontainers (PostgreSQL), Flyway, UPSERT, optimistic‑lock.
- E2E (локально): история → индикаторы → сигналы (без LIVE).

---

## 15) Производительность

- Батчи 500–2000, `fetchSize`/стриминг, `RETURNING` — только при необходимости.
- Партиции/индексы; регулярный `ANALYZE`.
- Профилировать: I/O к бирже, парсинг, запись в БД.

---

## 16) Политика версий Maven (properties)

У **каждой зависимости** — явная версия из `<properties>`. Допустим BOM Spring Boot, но версии пинятся свойствами.

```xml
<properties>
  <java.version>25</java.version>
  <spring.boot.version>3.5.0</spring.boot.version>
  <spring.security.version>6.3.0</spring.security.version>
  <jooq.version>3.19.9</jooq.version>
  <postgresql.version>42.7.4</postgresql.version>
  <flyway.version>10.17.0</flyway.version>
  <mapstruct.version>1.6.2</mapstruct.version>
  <lombok.version>1.18.34</lombok.version>
  <testcontainers.version>1.20.3</testcontainers.version>
  <resilience4j.version>2.2.0</resilience4j.version>
  <micrometer.version>1.13.2</micrometer.version>
  <springdoc.version>2.6.0</springdoc.version>
  <jackson.version>2.17.2</jackson.version>
  <caffeine.version>3.1.8</caffeine.version>
  <hibernate.validator.version>8.0.1.Final</hibernate.validator.version>
</properties>
```

---

## 17) Code Review — чек‑лист (jOOQ‑only)

- Только jOOQ? (нет JPA/ORM)
- Маршрут данных строго через **domain**?
- Иммутабельные таблицы — без апдейтов?
- UPSERT‑паттерн корректный? `UNIQUE`‑ключи отражают идемпотентность?
- Оптимистический апдейт там, где нужно? (`rows==1` + retry/backoff)
- Нет `Thread.sleep()`?
- Логи/метрики с `run_id` и тегами `(exchange,instrument,timeframe)`?
- Таймфреймы — только `OkxTimeframes.*`?
- Конфиги — **YAML**, без дефолтов в коде?
- Продовая БД — через **docker-compose.yml**?

---

## 18) Частые SQL‑паттерны (jOOQ)

```java
// Идемпотентная вставка
dsl.insertInto(TABLE)
   .set(record)
   .onConflict(TABLE.UNIQUE_KEY.fields())
   .doNothing()
   .execute();

// Идемпотентный upsert
dsl.insertInto(TABLE)
   .set(record)
   .onConflict(TABLE.UNIQUE_KEY.fields())
   .doUpdate()
   .set(TABLE.SOME_FIELD, DSL.excluded(TABLE.SOME_FIELD))
   .execute();

// Оптимистический апдейт
int rows = dsl.update(T)
    .set(T.STATUS, newStatus)
    .set(T.VERSION, T.VERSION.plus(1))
    .where(T.ID.eq(id).and(T.VERSION.eq(oldVersion)))
    .execute();
// rows == 1 ? OK : retry/backoff
```

---

## 19) Примечания

- Все даты/время — UTC. Источник цены для индикаторов (Close/HL2/HLC3/OHLC4) фиксируется и включается в канонический JSON параметров.
- Для больших выгрузок — курсоры/стрим, не грузить всё в память.
- README проекта — держать актуальные версии Java/Spring/jOOQ/Gradle/Maven.
