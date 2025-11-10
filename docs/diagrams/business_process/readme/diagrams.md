# Диаграммы бизнес‑процессов

Это индекс для **sequence**‑ и **flow**‑диаграмм проекта. В нём кратко описаны процессы и даны ссылки на исходники PlantUML.

> Все диаграммы используют общие фрагменты: `style.puml`, `legend.puml`, `lifelines.puml` (см. папку `_fragments/`).

---

## Как рендерить

* **PlantUML** ≥ 1.2023.6 или **Kroki**.
* Подключение фрагментов в каждой диаграмме:

  ```plantuml
  @startuml
  !include ../_fragments/style.puml
  !include ../_fragments/legend.puml
  !include ../_fragments/lifelines.puml

  BP_STYLE()
  BP_LIFELINES()
  BP_LEGEND()
  ...
  @enduml
  ```
* При пакетной сборке **исключайте** папку `_fragments` из рендера.

---

## Дерево каталогов

```
docs/
  diagrams/
    business_process/
      _fragments/      # общие include-файлы (стили, легенда, участники)
      sequence/        # последовательности (операционные сценарии)
      flows/           # блок‑схемы (L0/L1 процессы) — опционально
```

---

## Sequence‑диаграммы (операционные процессы)

Коротко о назначении каждой диаграммы. Ссылки ведут на исходники `.puml`.

1. **Registry: resolveOrCreate immutable *params (M2)**
   Файл: [`sequence/m02_configs_resolve_or_create.puml`](../sequence/m02_configs_resolve_or_create.puml)
   Назначение: канонизация JSON‑конфигов и реестр `*_params` (append‑only), возврат `params_id` для всех потребителей.

2. **Onboarding инструмента/биржи (M3/M4)**
   Файл: [`sequence/m03_onboarding_instrument.puml`](../sequence/m03_onboarding_instrument.puml)
   Назначение: добавление биржи/инструмента, первичная докачка истории, инициализация coverage.

3. **Инкрементальная докачка истории (M4/M5)**
   Файл: [`sequence/m04_history_ingestion.puml`](../sequence/m04_history_ingestion.puml)
   Назначение: регулярная подкачка закрытых свечей, обновление coverage.

4. **Целостность данных и автодокачка (M5)**
   Файл: [`sequence/m05_data_consistency_and_gapfill.puml`](../sequence/m05_data_consistency_and_gapfill.puml)
   Назначение: поиск «дыр», таргетированная докачка, контроль сетки времени и дублей.

5. **Расчёт значений индикаторов (M6–M7)**
   Файл: [`sequence/m06_m07_indicators_fill.puml`](../sequence/m06_m07_indicators_fill.puml)
   Назначение: вычисление EMA→… (прочих индикаторов), чекпоинты и идемпотентные вставки.

6. **Генерация сигналов (M8)**
   Файл: [`sequence/m08_signals_generate.puml`](../sequence/m08_signals_generate.puml)
   Назначение: преобразование значений индикаторов в сигналы, учёт фильтров/дебаунса, объяснимость (`reason`).

7. **Бэктест: оффлайн‑симуляция исполнения (M9)**
   Файл: [`sequence/m09_backtest_run.puml`](../sequence/m09_backtest_run.puml)
   Назначение: симуляция сигнал→ордера→позиции, агрегирование метрик (PNL, PF, Expectancy, DD…).

8. **Кворум: композитный score и решение (M10)**
   Файл: [`sequence/m10_quorum_decide.puml`](../sequence/m10_quorum_decide.puml)
   Назначение: агрегирование входов, gatekeepers, пороги действий (ENTER/EXIT/REVERSE/MODIFY/HOLD), журнал решений.

9. **Go/No‑Go LIVE: допуск и запуск сессии (M10→M11)**
   Файл: [`sequence/m10_m11_go_no_go_live.puml`](../sequence/m10_m11_go_no_go_live.puml)
   Назначение: проверка критериев по результатам M9, вайтлист, старт live‑сессии.

10. **LIVE: решение → ордера → позиция (M11)**
    Файл: [`sequence/m11_live_trading_session.puml`](../sequence/m11_live_trading_session.puml)
    Назначение: исполнение решений кворума, выставление/изменение/закрытие, учёт филлов и реконсиляция.

11. **Guardrails: авто‑пауза при серии убыточных (M11)**
    Файл: [`sequence/m11_guardrails_loss_streak.puml`](../sequence/m11_guardrails_loss_streak.puml)
    Назначение: вычисление лосс‑стрика, постановка на паузу, ручное возобновление.

12. **Реконсиляция состояний с биржей (M11)**
    Файл: [`sequence/m11_reconciliation.puml`](../sequence/m11_reconciliation.puml)
    Назначение: сверка ордеров/позиций/филлов и устранение расхождений.

13. **Пост‑трейд отчётность и трассировка причин**
    Файл: [`sequence/rpt_reporting_posttrade.puml`](../sequence/rpt_reporting_posttrade.puml)
    Назначение: отчёты P&L/метрики и полная трасса «индикаторы → сигналы → кворум → решения → ордера → сделки».

---

## Flow‑диаграммы (блок‑схемы)

> Опционально: L0/L1 схемы для высокоуровневого понимания и ручных процедур.

* **L0 Value Stream** — сквозной путь ценности: онбординг → история → индикаторы → сигналы → кворум → LIVE → отчёты.
  Файл: [`flows/l0_value_stream.puml`](../flows/l0_value_stream.puml)

* **Onboarding Flow (M3)** — проверка конфигов, реестр, первичная докачка.
  Файл: [`flows/m03_onboarding_flow.puml`](../flows/m03_onboarding_flow.puml)

* **Coverage & Ingestion (M4–M5)** — регулярная докачка, дырки, coverage.
  Файл: [`flows/m04_m05_coverage_and_ingestion_flow.puml`](../flows/m04_m05_coverage_and_ingestion_flow.puml)

* **Indicators (M6–M7)** — warmup, вставки, чекпоинты.
  Файл: [`flows/m06_m07_indicators_flow.puml`](../flows/m06_m07_indicators_flow.puml)

* **Signals (M8)** — выбор HTF/LTF, debounce/cooldown.
  Файл: [`flows/m08_signals_flow.puml`](../flows/m08_signals_flow.puml)

* **Quorum (M10)** — gatekeepers, весовая агрегация, пороги.
  Файл: [`flows/m10_quorum_decision_flow.puml`](../flows/m10_quorum_decision_flow.puml)

* **Backtest (M9)** — конфиг→симуляция→метрики.
  Файл: [`flows/m09_backtest_flow.puml`](../flows/m09_backtest_flow.puml)

* **Live Ordering (M11)** — решение→ордера→позиция.
  Файл: [`flows/m11_live_ordering_flow.puml`](../flows/m11_live_ordering_flow.puml)

* **Guardrails (M11)** — пауза при серии убытков.
  Файл: [`flows/m11_guardrails_flow.puml`](../flows/m11_guardrails_flow.puml)

* **Reconcile (M11)** — сверка и коррекция.
  Файл: [`flows/m11_reconcile_flow.puml`](../flows/m11_reconcile_flow.puml)

* **Go/No‑Go (M10→M11)** — критерии допуска и старт.
  Файл: [`flows/m10_m11_go_no_go_flow.puml`](../flows/m10_m11_go_no_go_flow.puml)

---

## Инварианты & стиль (коротко)

* **UTC** везде, только **закрытые свечи**.
* **Append‑only** таблицы, идемпотентные вставки, один писатель на серию (`exchange_instrument × timeframe`).
* Чёткая трассируемость: `indicator_values → signal_event → quorum_decision_event → order → fill → position_ledger`.
* Цвета: синий — нормальный поток, оранжевый — ретраи/лимиты, красный — ошибки/отказы (см. легенду).

---

Если добавить новые диаграммы — просто расширьте списки выше и придерживайтесь одинаковых неймингов (`mXX_*` и `flows/*`).
