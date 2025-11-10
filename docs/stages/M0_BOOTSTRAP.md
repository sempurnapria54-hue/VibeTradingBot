# M0_BOOTSTRAP.md — Бутстрап проекта

Версия: 1.0 • Цель — завести репозиторий со стабильным «скелетом»: сборка, профили, БД (docker-compose), jOOQ‑codegen, Security (JWT), springdoc (OpenAPI), базовые YAML‑конфиги и структура каталогов. **Без бизнес‑логики.**

---

## 1) Результат этапа (Definition of Done)

1. `docker-compose.yml` под PostgreSQL (prod/infra) + `.env.example` (без секретов).  
2. `pom.xml` с **всеми версиями в `<properties>`**, профилем `codegen` для jOOQ, подключёнными стартер‑зависимостями (web, security, flyway, jooq, …).  
3. Конфиги **YAML**: `application.yml`, `application-dev.yml`, `application-test.yml`, `application-prod.yml`.  
4. Настроен `springdoc-openapi-starter-webmvc-ui` → доступны `/swagger-ui` и `/v3/api-docs`.  
5. Настроен каркас Security: Stateless JWT (Bearer), роли `ROLE_ADMIN/ROLE_TRADER/ROLE_READER`, публичные `/auth/**`.  
6. Настроен `DSLContext` (jOOQ) + костяк **codegen** (maven‑плагин, профиль `codegen`).  
7. Прогоняются миграции Flyway при старте (`db/migration`).  
8. Проект стартует локально, `/actuator/health` = UP; в prod‑профиле приложение подключается к БД из compose.  

---

## 2) Структура каталогов (скелет)

```
trading-bot/
├─ docker-compose.yml
├─ .env.example
├─ pom.xml
├─ README.md
├─ docs/
│  ├─ TECH_STYLE_GUIDE.md
│  └─ stages/
│     ├─ M0_BOOTSTRAP.md
│     └─ ...
├─ src/
│  ├─ main/
│  │  ├─ java/com/example/tradingbot/
│  │  │  ├─ api/           # REST DTO + контроллеры (тонкие, v1)
│  │  │  ├─ client/        # коннекторы бирж и их модели
│  │  │  ├─ config/        # @Configuration, SecurityConfig, OpenAPIConfig, jOOQConfig
│  │  │  ├─ domain/        # rich model + сервисы домена
│  │  │  ├─ mapping/       # MapStruct‑мапперы
│  │  │  ├─ persistence/   # jOOQ DAO/репозитории + generated (исключить из VCS)
│  │  │  ├─ rest/          # контроллеры
│  │  │  └─ util/
│  │  └─ resources/
│  │     ├─ application.yml
│  │     ├─ application-dev.yml
│  │     ├─ application-test.yml
│  │     └─ application-prod.yml
│  └─ test/java/...        # unit + integration (Testcontainers)
└─ db/
   └─ migration/           # Flyway V__*.sql с комментариями
```

> Папку `persistence/jooq/generated` держать вне VCS (добавить в `.gitignore`), генерить по профилю `codegen`.

---

## 3) `docker-compose.yml` (PostgreSQL) + `.env.example`

**Файл: `docker-compose.yml` (корень репозитория)**

```yaml
version: "3.9"
services:
  db:
    image: postgres:15-alpine
    container_name: tradingbot-db
    restart: unless-stopped
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      PGDATA: /var/lib/postgresql/data/pgdata
    ports:
      - "5432:5432"
    volumes:
      - dbdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $$POSTGRES_USER -d $$POSTGRES_DB"]
      interval: 10s
      timeout: 5s
      retries: 5
volumes:
  dbdata:
```

**Файл: `.env.example` (скопировать в `.env`, не коммитить содержимое с реальными секретами)**

```
DB_NAME=tradingbot
DB_USER=postgres
DB_PASSWORD=postgres
```

> В prod‑профиле приложение подключается по `jdbc:postgresql://db:5432/${DB_NAME}` (см. ниже YAML).

---

## 4) YAML‑конфиги Spring

**`application.yml` (база, без секретов)**

```yaml
server:
  port: 8080

spring:
  application:
    name: trading-bot
  jooq:
    sql-dialect: POSTGRES
  flyway:
    enabled: true
    locations: classpath:db/migration

management:
  endpoints:
    web:
      exposure:
        include: health,info

logging:
  level:
    com.example.tradingbot: INFO

springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
    path: /swagger-ui
  paths-to-match: /api/**, /auth/**

security:
  jwt:
    issuer: "trading-bot"
    access-token-ttl: PT15M
    refresh-token-ttl: P14D
```

**`application-dev.yml` (локальная разработка)**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tradingbot
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
logging:
  level:
    com.example.tradingbot: DEBUG
```

**`application-test.yml` (интеграционные тесты, Testcontainers)**

```yaml
spring:
  datasource:
    url: jdbc:tc:postgresql:15:///tradingbot
  flyway:
    enabled: true
logging:
  level:
    root: WARN
```

**`application-prod.yml` (compose‑сеть, контейнер `db`)**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://db:5432/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
logging:
  level:
    root: INFO
```

---

## 5) `pom.xml` — свойства версий, зависимости, профили

**Требование:** _каждая зависимость_ указывает версию через `<properties>`. Разрешается импортировать BOM Spring Boot, но версии всё равно пинятся.

Мини‑скелет (фрагменты):

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
  <springdoc.version>2.6.0</springdoc.version>
  <testcontainers.version>1.20.3</testcontainers.version>
</properties>

<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-dependencies</artifactId>
      <version>${spring.boot.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>${spring.boot.version}</version>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
    <version>${spring.boot.version}</version>
  </dependency>
  <dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
    <version>${flyway.version}</version>
  </dependency>
  <dependency>
    <groupId>org.jooq</groupId>
    <artifactId>jooq</artifactId>
    <version>${jooq.version}</version>
  </dependency>
  <dependency>
    <groupId>org.jooq</groupId>
    <artifactId>jooq-meta</artifactId>
    <version>${jooq.version}</version>
  </dependency>
  <dependency>
    <groupId>org.jooq</groupId>
    <artifactId>jooq-codegen</artifactId>
    <version>${jooq.version}</version>
  </dependency>
  <dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>${postgresql.version}</version>
  </dependency>
  <dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>${springdoc.version}</version>
  </dependency>
  <dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>${lombok.version}</version>
    <scope>provided</scope>
  </dependency>
  <dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>${mapstruct.version}</version>
  </dependency>

  <!-- Test -->
  <dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>${testcontainers.version}</version>
    <scope>test</scope>
  </dependency>
</dependencies>
```

**Профиль `codegen` для jOOQ** (пример; JDBC берём из ENV, схема — `public`):

```xml
<profiles>
  <profile>
    <id>codegen</id>
    <build>
      <plugins>
        <plugin>
          <groupId>org.jooq</groupId>
          <artifactId>jooq-codegen-maven</artifactId>
          <version>${jooq.version}</version>
          <executions>
            <execution>
              <goals>
                <goal>generate</goal>
              </goals>
            </execution>
          </executions>
          <configuration>
            <jdbc>
              <driver>org.postgresql.Driver</driver>
              <url>${env.DB_URL}</url>
              <user>${env.DB_USER}</user>
              <password>${env.DB_PASSWORD}</password>
            </jdbc>
            <generator>
              <database>
                <name>org.jooq.meta.postgres.PostgresDatabase</name>
                <inputSchema>public</inputSchema>
              </database>
              <generate>
                <pojos>true</pojos>
                <daos>false</daos>
              </generate>
              <target>
                <packageName>com.example.tradingbot.persistence.jooq.generated</packageName>
                <directory>src/main/java</directory>
              </target>
            </generator>
          </configuration>
        </plugin>
      </plugins>
    </build>
  </profile>
</profiles>
```

---

## 6) Security (JWT) и OpenAPI

**Security (каркас):**
- Stateless: `SessionCreationPolicy.STATELESS`, CSRF off, CORS явно.  
- Публичные эндпоинты: `/auth/**`, `/actuator/health`, `/v3/api-docs/**`, `/swagger-ui/**`.  
- Роли: `ROLE_ADMIN`, `ROLE_TRADER`, `ROLE_READER`.  
- JWT: RS256 (RSA‑ключи в Secret Manager). Эндпоинты: `POST /auth/login`, `POST /auth/refresh`. Пароли — BCrypt ≥ 12.

**OpenAPI (springdoc):**
- Стартер подключён; добавить конфигурацию Bearer:
  - securityScheme `bearerAuth` (JWT)
  - global `security` requirement для `/api/**`
- В prod ограничить доступ к Swagger через Security или отключить по профилю.

---

## 7) jOOQ: `DSLContext` и соглашения

- Один бин `DSLContext`, провайдер HikariCP.  
- Репозитории — тонкие: только SQL и маппинг persistence ↔ domain (через MapStruct/фабрики).  
- Генерация артефактов — `mvn -Pcodegen jooq-codegen:generate` (при активной БД и миграциях).

---

## 8) Команды и порядок запуска

```bash
# Поднять БД (prod/infra)
cp .env.example .env
docker compose up -d db

# Применить миграции (приложение делает это само на старте)
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Сгенерировать jOOQ‑артефакты (при поднятой БД и применённых миграциях)
export DB_URL=jdbc:postgresql://localhost:5432/tradingbot
export DB_USER=postgres
export DB_PASSWORD=postgres
./mvnw -Pcodegen jooq-codegen:generate

# Dev‑запуск
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

После старта проверить:
- `GET /actuator/health` → `{"status":"UP"}`  
- `GET /swagger-ui` доступен; `/v3/api-docs` работает  
- защищённые `/api/**` → 401 без токена

---

## 9) Acceptance‑критерии проверки

- Проект собирается на Java 25, Boot 3.5.x.  
- В prod‑профиле подключается к БД из compose; Flyway применяет миграции.  
- jOOQ‑codegen генерит пакеты в `persistence.jooq.generated`.  
- Security: `/auth/**` доступны, `/api/**` требуют JWT (401 без него).  
- Swagger доступен в dev/prod (по политике), Bearer схема подключена.  
- Все версии зависимостей — в `<properties>` POM.  
- Конфиги только YAML; никаких `.properties`.

---

## 10) Не входит в M0 (в следующих этапах)

- Доменные модели и мапперы индикаторов/сигналов (M1/M6/M7).  
- Исторические данные и докачка (M4/M5).  
- Бэктест/симулятор/кворум/ордерный контур (M9–M11).

---

## 11) Чек‑лист ревью (для MR/PR)

- [ ] `docker-compose.yml` без секретов; `.env.example` присутствует.  
- [ ] Все версии зависимостей указаны в `<properties>`.  
- [ ] Конфиги в YAML, профили dev/test/prod настроены.  
- [ ] Поднят springdoc, настроена Bearer‑схема.  
- [ ] Security stateless, публичные пути корректны.  
- [ ] Настроен профиль `codegen`, jOOQ генерит код.  
- [ ] `persistence/jooq/generated` исключён из VCS.  
- [ ] Приложение стартует и отдаёт `/actuator/health`.  
