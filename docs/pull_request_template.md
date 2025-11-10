### Этап: M{N} — {название}
- [ ] Соответствие /specs/README.md и /specs/docs/stages/M{N}_*.md
- [ ] Java 25, Spring Boot 3.x, jOOQ-only, Lombok, MapStruct
- [ ] Конфиги: application.yaml, docker-compose.yaml (PostgreSQL)
- [ ] Все версии зависимостей в <properties> (pom.xml)
- [ ] Swagger: springdoc-openapi-starter-webmvc-ui
- [ ] Security: JWT (dev можно упростить)
- [ ] Модели client/domain/api/persistence → маппинг только через domain
- [ ] Без триггеров БД; вставки идемпотентны; канонические таймфреймы
